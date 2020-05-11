package com.mashup.nnaa.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mashup.nnaa.R;
import com.mashup.nnaa.data.Choices;
import com.mashup.nnaa.main.MainActivity;
import com.mashup.nnaa.network.RetrofitHelper;
import com.mashup.nnaa.network.model.AdditionalProp;
import com.mashup.nnaa.network.model.Questionnaire;
import com.mashup.nnaa.network.model.Questions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SharingAdapter extends RecyclerView.Adapter<SharingAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "SharingAdapter";

    private Context sContext;
    private ArrayList<Questionnaire> unFilterdList;
    private ArrayList<Questionnaire> filteredList;
    private String id = AccountManager.getInstance().getUserAuthHeaderInfo().getUserId();
    private String token = AccountManager.getInstance().getUserAuthHeaderInfo().getToken();
    private long now = System.currentTimeMillis();
    private Date date = new Date(now);
    private SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.KOREA);
    private String time = simple.format(date);
    ArrayList<String> keyList = new ArrayList<>();
    ArrayList<String> valueList = new ArrayList<>();

    public SharingAdapter(Context context, ArrayList<Questionnaire> list) {
        this.unFilterdList = list;
        this.filteredList = list;
        this.sContext = context;
    }

    public void setSharinglist(ArrayList<Questionnaire> list) {
        this.filteredList = list;
        notifyDataSetChanged();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {

                } else {
                    ArrayList<Questionnaire> filteringList = new ArrayList<>();
                    for (Questionnaire name : filteredList) {
                        if (name.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(name);
                        }
                    }
                    for (Questionnaire email : filteredList) {
                        if (email.getEmail().toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(email);
                        }
                    }
                    filteredList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (ArrayList<Questionnaire>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(sContext).inflate(R.layout.activity_sharing_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_name.setText(filteredList.get(position).getName());
        holder.txt_email.setText(filteredList.get(position).getEmail());


        Bundle bundle = ((Activity) sContext).getIntent().getExtras();
        final String category = bundle.getString("category");
        final String list = bundle.getString("list");


        int pos = holder.getAdapterPosition();

        Questionnaire item = filteredList.get(pos);
        JSONObject additionalObject = new JSONObject();
        JSONObject choiceObject = new JSONObject();
        JSONObject questionObject = new JSONObject();
        JSONObject test = new JSONObject();

        Questions questions= new Questions();
        AdditionalProp additionalProp = new AdditionalProp();
        Choices choices = new Choices();



        holder.userSelect.setOnClickListener(view -> {

            try {
                JSONArray array = new JSONArray(list);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    test.put("additionalProp" + (i + 1), object);

                    String choice = object.getString("choices");
                    if (array.getJSONObject(i).isNull("choices")) {
                        choice = null;
                    }

                    if(choice.equals("null")) {
                        choice = object.optString("null");
                    }

                    String a = object.optString("a");
                    String b = object.optString("b");
                    String c= object.optString("c");
                    String d= object.optString("d");
                    String content = object.getString("content");
                    String type = object.getString("type");


                    choices.setA(a);
                    choices.setB(b);
                    choices.setC(c);
                    choices.setD(d);

                    choiceObject.put("choices", choice);
                    additionalObject.put("additionalProp"+(i+1), choiceObject);
                    additionalObject.put("content", content);
                    additionalObject.put("type", type);

                    test.put("additionalProp"+(i+1),object);

                    additionalProp.setChoices(choices);
                    additionalProp.setType(type);
                    additionalProp.setContent(content);
                    //questions.setAdditionalProp(additionalProp);
                    //questions.setAdditionalProp(test);
                }
                String json = test.toString();
                Log.d(TAG, "제이슨 확인: json = " + json);

                Questionnaire questionnaire = new Questionnaire(category, time, questions, item.getId());

                RetrofitHelper.getInstance().postQuestionnaire(id, token, questionnaire, new Callback<Questionnaire>() {
                    @Override
                    public void onResponse(Call<Questionnaire> call, Response<Questionnaire> response) {

                        if(response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "질문지를 보내겠습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(view.getContext(), MainActivity.class);
                            sContext.startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<Questionnaire> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });


//                    if (choice.equals("'null'")) {
//                        JSONObject nullobject = new JSONObject(choice);
//                        String nullvalue = nullobject.getString("null");
//                        choices.setNullValue(nullvalue);
//                        additionalProp.setContent(content);
//                        additionalProp.setType(type);
//                        additionalProp.setChoices(choices);
//                        questions.setAdditionalProp(additionalProp);
//                    }
//                    if (!choice.equals("null") && !type.equals("null") && !content.equals("null")) {
//                        JSONObject choiceobject = new JSONObject(choice);
//                        String a = choiceobject.getString("a");
//                        String b = choiceobject.getString("b");
//                        String c = choiceobject.getString("c");
//                        String d = choiceobject.getString("d");
//                        Log.d(TAG, "제이슨 확인: abcd = " + a + b + c + d);
//                        choices.setA(a);
//                        choices.setB(b);
//                        choices.setC(c);
//                        choices.setD(d);
//                        additionalProp.setContent(content);
//                        additionalProp.setType(type);
//                        additionalProp.setChoices(choices);
//                        questions.setAdditionalProp(additionalProp);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }


    @Override
    public int getItemCount() {
        if (filteredList != null) {
            return filteredList.size();
        } else return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txt_name, txt_email;
        Button userSelect;

        public ViewHolder(View itemView) {
            super(itemView);

            txt_name = itemView.findViewById(R.id.sharing_txt_name);
            txt_email = itemView.findViewById(R.id.sharing_txt_email);
            userSelect = itemView.findViewById(R.id.user_select);
        }
    }
}
