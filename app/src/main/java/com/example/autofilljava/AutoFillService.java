package com.example.autofilljava;

import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import android.service.autofill.FillResponse;
import android.service.autofill.Dataset;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AutoFillService extends AutofillService {
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onFillRequest(@NonNull FillRequest fillRequest, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback fillCallback) {
        // Get the structure from the request
        List<FillContext> context = fillRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Parse the structure into fillable view IDs
        StructureParser.Result parseResult = new StructureParser(structure).parse();

        RemoteViews remoteView = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);

        // Add a dataset to the response
        FillResponse.Builder fillResponseBuilder = new FillResponse.Builder();

        for (String domain: parseResult.webDomain) {
            System.out.println("Domain: " + domain);
        }

        String username = "test@buttercup.pw";
        String password = "testpassword";

        remoteView.setTextViewText(android.R.id.text1, "Autofill using " + username);
        Dataset.Builder builder = new Dataset.Builder(remoteView);

        // Assign the username/password to any found view IDs
        parseResult.email.forEach(id -> builder.setValue(id, AutofillValue.forText(username)));
        parseResult.username.forEach(id -> builder.setValue(id, AutofillValue.forText(username)));
        parseResult.password.forEach(id -> builder.setValue(id, AutofillValue.forText(password)));
        try {
            Dataset dataSet = builder.build();
            fillResponseBuilder.addDataset(dataSet);
            fillCallback.onSuccess(fillResponseBuilder.build());
        } catch (Exception e) {
            fillCallback.onSuccess(null);
        }
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest saveRequest, @NonNull SaveCallback saveCallback) {

    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
    }

    void identifyEmailFields(AssistStructure.ViewNode node,
                             List<AssistStructure.ViewNode> emailFields) {
        if(node.getClassName().contains("EditText")) {
            String viewId = node.getIdEntry();
            if(viewId!=null && (viewId.contains("email")
                    || viewId.contains("username"))) {
                emailFields.add(node);
                return;
            }
        }

        for(int i=0; i<node.getChildCount();i++) {
            identifyEmailFields(node.getChildAt(i), emailFields);
        }
    }
}
