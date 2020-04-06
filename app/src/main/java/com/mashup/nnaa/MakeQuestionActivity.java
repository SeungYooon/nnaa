package com.mashup.nnaa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.mashup.nnaa.network.RetrofitHelper;
import com.mashup.nnaa.network.model.NewQuestionDto;
import com.mashup.nnaa.question.QuestionActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeQuestionActivity extends AppCompatActivity {

    Button CustomDone;
    ImageButton Custom_OX, Custom_J_Blue, Custom_G_Blue, Custom_OX_Blue, Custom_J, Custom_G;
    TextView txtJ, txtG, txtOX;
    EditText Content_Edit, FirstEdit, SecondEdit, ThirdEdit, ForthEdit;
    Spinner spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_question);

        CustomDone = findViewById(R.id.custom_done);
        Custom_J = findViewById(R.id.custom_btn_j);
        Custom_G = findViewById(R.id.custom_btn_g);
        Custom_OX = findViewById(R.id.custom_btn_ox);

        Custom_J_Blue = findViewById(R.id.custom_btn_j_blue);
        Custom_G_Blue = findViewById(R.id.custom_btn_g_blue);
        Custom_OX_Blue = findViewById(R.id.custom_btn_ox_blue);
        spinnerCategory = findViewById(R.id.spinner_category);

        txtJ = findViewById(R.id.txt_j);
        txtG = findViewById(R.id.txt_g);
        txtOX = findViewById(R.id.txt_o);
        Content_Edit = findViewById(R.id.custom_first_edit);
        FirstEdit = findViewById(R.id.custom_choice_edit1);
        SecondEdit = findViewById(R.id.custom_choice_edit2);
        ThirdEdit = findViewById(R.id.custom_choice_edit3);
        ForthEdit = findViewById(R.id.custom_choice_edit4);

        NewQuestionDto newQu = new NewQuestionDto();

        Custom_J.setOnClickListener(view -> {
            Custom_J.setVisibility(View.INVISIBLE);
            Custom_J_Blue.setVisibility(View.VISIBLE);
            Custom_OX.setEnabled(false);
            Custom_G.setEnabled(false);
            Toast.makeText(view.getContext(), "주관식 선택!", Toast.LENGTH_SHORT).show();
            newQu.setType("주관식");
        });
        Custom_J_Blue.setOnClickListener(view -> {
            Custom_J_Blue.setVisibility(View.INVISIBLE);
            Custom_J.setVisibility(View.VISIBLE);
            Custom_OX_Blue.setEnabled(true);
            Custom_G.setEnabled(true);
            Toast.makeText(view.getContext(), "주관식 선택 취소!", Toast.LENGTH_SHORT).show();
            newQu.setType("");
        });
        Custom_G.setOnClickListener(view -> {
            Custom_G.setVisibility(View.INVISIBLE);
            Custom_G_Blue.setVisibility(View.VISIBLE);
            Custom_J.setEnabled(false);
            Custom_OX.setEnabled(false);
            Toast.makeText(view.getContext(), "객관식 선택!", Toast.LENGTH_SHORT).show();
            newQu.setType("객관식");
        });
        Custom_G_Blue.setOnClickListener(view -> {
            Custom_G_Blue.setVisibility(View.INVISIBLE);
            Custom_G.setVisibility(View.VISIBLE);
            Custom_J.setEnabled(true);
            Custom_OX.setEnabled(true);
            Toast.makeText(view.getContext(), "객관식 선택 취소!", Toast.LENGTH_SHORT).show();
            newQu.setType("");
        });
        Custom_OX.setOnClickListener(view -> {
            Custom_OX.setVisibility(View.INVISIBLE);
            Custom_OX_Blue.setVisibility(View.VISIBLE);
            Custom_G.setEnabled(false);
            Custom_J.setEnabled(false);
            Toast.makeText(view.getContext(), "O X 선택!", Toast.LENGTH_SHORT).show();
            newQu.setType("OX");
        });
        Custom_OX_Blue.setOnClickListener(view -> {
            Custom_OX_Blue.setVisibility(View.INVISIBLE);
            Custom_OX.setVisibility(View.VISIBLE);
            Custom_J.setEnabled(true);
            Custom_G.setEnabled(true);
            Toast.makeText(view.getContext(), "O X 선택 취소!", Toast.LENGTH_SHORT).show();
            newQu.setType("");
        });

        CustomDone.setOnClickListener(view -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("a", FirstEdit.getText().toString());
                    jsonObject.addProperty("b", SecondEdit.getText().toString());
                    jsonObject.addProperty("c", ThirdEdit.getText().toString());
                    jsonObject.addProperty("d", ForthEdit.getText().toString());
                    newQu.setContent(Content_Edit.getText().toString());
                    newQu.setCategory(spinnerCategory.getSelectedItem().toString());
                    newQu.setChoices(jsonObject);
                    RetrofitHelper.getInstance().postQuestion(newQu, new Callback<NewQuestionDto>() {
                        @Override
                        public void onResponse(Call<NewQuestionDto> call, Response<NewQuestionDto> response) {
                            if (response.isSuccessful()) {
                                Log.v("질문 직접 생성", String.valueOf(response.code()));
                                CustomDone.setBackgroundColor(Color.BLUE);
                                launchQuestionActivity();
                            } else if (response.code() == 400) {
                                Toast.makeText(MakeQuestionActivity.this, "질문 세팅을 완료해주세요!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<NewQuestionDto> call, Throwable t) {
                            Log.v("질문 직접 생성 실패", t.getMessage());
                        }
                    });
                }
        );
    }

    private void launchQuestionActivity() {
        Toast.makeText(MakeQuestionActivity.this, "질문 작성 완료!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MakeQuestionActivity.this, QuestionActivity.class);
        startActivity(intent);
        finish();
    }
}
