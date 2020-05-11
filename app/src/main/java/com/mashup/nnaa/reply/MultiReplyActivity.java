package com.mashup.nnaa.reply;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.JsonParser;
import com.mashup.nnaa.R;
import com.mashup.nnaa.main.MainActivity;
import com.mashup.nnaa.network.RetrofitHelper;
import com.mashup.nnaa.network.model.Answers;
import com.mashup.nnaa.network.model.NewQuestionDto;
import com.mashup.nnaa.network.model.QuestionnaireAnswerDto;
import com.mashup.nnaa.util.AccountManager;
import com.mashup.nnaa.util.ReplyAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiReplyActivity extends AppCompatActivity {

    private static final String TAG = "MultiReplyActivity";
    TextView reply_number, reply_end_nubmer, txtA, txtB, txtC, txtD, a_txt, b_txt, c_txt, d_txt, txtQuestion;
    ImageButton reply_cancel, reply_choice, reply_O, reply_X, btnA, btnB, btnC, btnD;
    ImageView ox_bar, multi_img1, multi_img2, multi_img3, multi_img4, btn_past;
    Button btn_next_question;
    EditText replyEdit;
    ScrollView scrollView;
    private ReplyAdapter replyAdapter;
    private ArrayList<NewQuestionDto> questionDtoList;
    private int count = 1;
    private long now = System.currentTimeMillis();
    private Date date = new Date(now);
    private SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.KOREA);
    private String time = simple.format(date);
    String id = AccountManager.getInstance().getUserAuthHeaderInfo().getUserId();
    String token = AccountManager.getInstance().getUserAuthHeaderInfo().getToken();
    LottieAnimationView lottieAnimationView, lottieStart, lottieClick;
    HashMap<String, String> answers = new HashMap<>();
    String a = "";
    String b = "";
    String c = "";
    String d = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_reply);

        txtQuestion = findViewById(R.id.text_question);
        reply_number = findViewById(R.id.reply_number);
        txtA = findViewById(R.id.txt_A);
        txtB = findViewById(R.id.txt_B);
        txtC = findViewById(R.id.txt_C);
        txtD = findViewById(R.id.txt_D);
        a_txt = findViewById(R.id.mutli_txt1);
        b_txt = findViewById(R.id.mutli_txt2);
        c_txt = findViewById(R.id.mutli_txt3);
        d_txt = findViewById(R.id.mutli_txt4);
        btnA = findViewById(R.id.multi_btn_a);
        btnB = findViewById(R.id.multi_btn_b);
        btnC = findViewById(R.id.multi_btn_c);
        btnD = findViewById(R.id.multi_btn_d);
        multi_img1 = findViewById(R.id.multi_img1);
        multi_img2 = findViewById(R.id.multi_img2);
        multi_img3 = findViewById(R.id.multi_img3);
        multi_img4 = findViewById(R.id.multi_img4);
        reply_O = findViewById(R.id.reply_o_btn);
        reply_X = findViewById(R.id.reply_x_btn);
        ox_bar = findViewById(R.id.reply_ox_bar);
        reply_end_nubmer = findViewById(R.id.reply_end_number);
        reply_cancel = findViewById(R.id.reply_cancel);
        btn_past = findViewById(R.id.btn_past);
        reply_choice = findViewById(R.id.reply_choice);
        btn_next_question = findViewById(R.id.btn_next_question);
        replyEdit = findViewById(R.id.reply_edit);
        lottieAnimationView = findViewById(R.id.lottie_complete);
        lottieStart = findViewById(R.id.lottie_start);
        lottieStart.bringToFront();
        lottieClick = findViewById(R.id.lottie_click);
        lottieClick.bringToFront();
        scrollView = findViewById(R.id.scroll_view);

        lottieAnimationView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("답변 완료");
            builder.setMessage("답변을 완료하셨나요?");
            builder.setPositiveButton("확인", (dialogInterface, i) -> {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            });
            builder.setNegativeButton("취소", (dialogInterface, i) -> Toast.makeText(getApplicationContext(), "답변을 완료해주세요.", Toast.LENGTH_SHORT).show());
            builder.show();
        });

        reply_cancel.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("답변 중단하기");
            builder.setMessage("답변을 중단하실건가요?");
            builder.setPositiveButton("네", (dialogInterface, i) -> {
                Toast.makeText(getApplicationContext(), "답변을 중단하겠습니다.", Toast.LENGTH_SHORT).show();
                finish();
            });
            builder.setNegativeButton("아니요", (dialogInterface, i) -> Toast.makeText(getApplicationContext(), "계속 답변해주세요~", Toast.LENGTH_SHORT).show());
            builder.show();
        });

        // ui 키보드에 밀리는거 방지
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        questionDtoList = new ArrayList<>();
        this.answerQuestion();
    }

    private void answerQuestion() {

        Intent intent = getIntent();
        String question = intent.getStringExtra("list");
        String q_id = intent.getStringExtra("id");

        reply_number.setText(String.valueOf(count));

        lottieClick.setOnClickListener(view -> {

            lottieStart.setVisibility(View.GONE);
            lottieClick.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(Objects.requireNonNull(question));

                for (int i = 0; i < object.length(); i++) {

                    reply_end_nubmer.setText(String.valueOf(object.length()));

                    JSONObject jsonObject = object.getJSONObject("additionalProp1");
                    String content = jsonObject.getString("content");
                    String type = jsonObject.getString("type");
                    boolean bookmark = jsonObject.getBoolean("isBookmarked");
                    txtQuestion.setText(content);

                    if (bookmark == true) {
                        reply_choice.setBackgroundResource(R.drawable.choice_btn_heart_on);
                    } else {
                        reply_choice.setBackgroundResource(R.drawable.choice_btn_heart_off);
                    }

                    if (jsonObject.getJSONObject("choices").isNull("choices")) {
                        String choice = jsonObject.getString("choices");
                        JSONObject c_object = new JSONObject(choice);
                        a = c_object.getString("a");
                        b = c_object.getString("b");
                        c = c_object.getString("c");
                        d = c_object.getString("d");
                        a_txt.setText(a);
                        b_txt.setText(b);
                        c_txt.setText(c);
                        d_txt.setText(d);
                    }
                    if (type.equals("객관식")) {
                        chooesQuestion();
                    } else if (type.equals("주관식")) {
                        subjectQuestion();
                    } else if (type.equals("OX")) {
                        oxQuestion();
                    }
                }
                btn_next_question.setOnClickListener(view1 -> {

                    QuestionnaireAnswerDto answerDto = new QuestionnaireAnswerDto(answers, time);
                    RetrofitHelper.getInstance().answerQuestionnaire(id, token, q_id, answerDto, new Callback<QuestionnaireAnswerDto>() {
                        @Override
                        public void onResponse(Call<QuestionnaireAnswerDto> call, Response<QuestionnaireAnswerDto> response) {
                            if (response.isSuccessful()) {
                                count++;
                                reply_number.setText(String.valueOf(count));

                                replyEdit.setText("");
                                reply_X.setImageResource(R.drawable.quiz_btn_x);
                                reply_O.setImageResource(R.drawable.group_3);
                                btnA.setBackgroundResource(R.drawable.multi_reply_oval);
                                txtA.setTextColor(getResources().getColor(R.color.darkergrey));
                                btnB.setBackgroundResource(R.drawable.multi_reply_oval);
                                txtB.setTextColor(getResources().getColor(R.color.darkergrey));
                                btnC.setBackgroundResource(R.drawable.multi_reply_oval);
                                txtC.setTextColor(getResources().getColor(R.color.darkergrey));
                                btnD.setBackgroundResource(R.drawable.multi_reply_oval);
                                txtD.setTextColor(getResources().getColor(R.color.darkergrey));

                                btn_next_question.setEnabled(true);
                                btn_past.setEnabled(true);

                                if (count == object.length()) {
                                    Toast.makeText(MultiReplyActivity.this, "마지막 질문입니다!", Toast.LENGTH_SHORT).show();
                                    btn_next_question.setEnabled(false);
                                    btn_past.setEnabled(true);
                                    lottieAnimationView.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<QuestionnaireAnswerDto> call, Throwable t) {
                            Log.v("@@@@@", Objects.requireNonNull(t.getMessage()));
                        }
                    });
                });

                btn_past.setOnClickListener(view2 -> {
                    if (count == 1) {
                        btn_past.setEnabled(false);
                        btn_next_question.setEnabled(true);
                        Toast.makeText(MultiReplyActivity.this, "첫번째 질문입니다!", Toast.LENGTH_SHORT).show();
                    } else {
                        count--;
                        reply_number.setText(String.valueOf(count));
                        btn_past.setEnabled(true);
                        btn_next_question.setEnabled(true);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

       /* RetrofitHelper.getInstance().getQuestion(id, token, "엄마", "10", new Callback<ArrayList<NewQuestionDto>>() {
            @Override
            public void onResponse(Call<ArrayList<NewQuestionDto>> call, Response<ArrayList<NewQuestionDto>> response) {
                if (questionDtoList != null) {
                    questionDtoList = response.body();

                    reply_number.setText(String.valueOf(count));
                    txtQuestion.setText(response.body().get(count - 1).getContent());
                    btn_next_question.setOnClickListener(view -> {
                        count++;
                        replyEdit.setText("");
                        reply_X.setImageResource(R.drawable.quiz_btn_x);
                        reply_O.setImageResource(R.drawable.group_3);
                        btnA.setBackgroundResource(R.drawable.multi_reply_oval);
                        txtA.setTextColor(getResources().getColor(R.color.darkergrey));
                        btnB.setBackgroundResource(R.drawable.multi_reply_oval);
                        txtB.setTextColor(getResources().getColor(R.color.darkergrey));
                        btnC.setBackgroundResource(R.drawable.multi_reply_oval);
                        txtC.setTextColor(getResources().getColor(R.color.darkergrey));
                        btnD.setBackgroundResource(R.drawable.multi_reply_oval);
                        txtD.setTextColor(getResources().getColor(R.color.darkergrey));

                        reply_number.setText(String.valueOf(count));
                        btn_next_question.setEnabled(true);
                        btn_past.setEnabled(true);
                        txtQuestion.setText(response.body().get(count - 1).getContent());
                        if (response.body().get(count - 1).getType() != null) {

                            switch (response.body().get(count - 1).getType()) {
                                case "주관식":
                                    subjectQuestion();
                                    break;
                                case "OX":
                                    oxQuestion();
                                    break;
                                case "객관식":
                                    chooesQuestion();

                                    break;
                            }
                        }
                        if (response.body().get(count - 1).isBookmarked()) {
                            reply_choice.setBackgroundResource(R.drawable.choice_btn_heart_on);
                        } else {
                            reply_choice.setBackgroundResource(R.drawable.choice_btn_heart_off);
                        }
                        if (count == questionDtoList.size()) {
                            Toast.makeText(MultiReplyActivity.this, "마지막 질문입니다!", Toast.LENGTH_SHORT).show();
                            btn_next_question.setEnabled(false);
                            btn_past.setEnabled(true);
                            lottieAnimationView.setVisibility(View.VISIBLE);
                        }
                    });
                    btn_past.setOnClickListener(view -> {
                        if (count == 1) {
                            btn_past.setEnabled(false);
                            btn_next_question.setEnabled(true);
                            Toast.makeText(MultiReplyActivity.this, "첫번째 질문입니다!", Toast.LENGTH_SHORT).show();
                        } else {
                            count--;
                            reply_number.setText(String.valueOf(count));
                            btn_past.setEnabled(true);
                            btn_next_question.setEnabled(true);
                            txtQuestion.setText(response.body().get(count - 1).getContent());
                            if (response.body().get(count - 1).getType() != null) {
                                switch (response.body().get(count - 1).getType()) {
                                    case "주관식":
                                        subjectQuestion();
                                        break;
                                    case "OX":
                                        oxQuestion();
                                        break;
                                    case "객관식":
                                        chooesQuestion();
                                        break;
                                }
                            }
                        }
                        if (response.body().get(count - 1).isBookmarked()) {
                            reply_choice.setBackgroundResource(R.drawable.choice_btn_heart_on);
                        } else {
                            reply_choice.setBackgroundResource(R.drawable.choice_btn_heart_off);
                        }
                    });
                    reply_end_nubmer.setText(String.valueOf(questionDtoList.size()));
                    Log.v("답변 리스트", "Response =  " + response.code() + "," + "id:" + "," + "token: " + token);
                    //replyAdapter.setQuestionDtoList(questionDtoList);
                } else if (questionDtoList.size() == 0) {
                    Toast.makeText(MultiReplyActivity.this, "질문을 생성해주세요!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.v("답변 리스트", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<NewQuestionDto>> call, Throwable t) {
                Log.v("답변 리스트", "에러:" + t.getMessage());
            }
        });*/
    }

    private void subjectQuestion() {
        replyEdit.setVisibility(View.VISIBLE);
        answers.put("additionalProp1", replyEdit.getText().toString());
        reply_X.setVisibility(View.INVISIBLE);
        reply_O.setVisibility(View.INVISIBLE);
        ox_bar.setVisibility(View.INVISIBLE);
        txtA.setVisibility(View.INVISIBLE);
        txtB.setVisibility(View.INVISIBLE);
        txtC.setVisibility(View.INVISIBLE);
        txtD.setVisibility(View.INVISIBLE);
        a_txt.setVisibility(View.INVISIBLE);
        b_txt.setVisibility(View.INVISIBLE);
        c_txt.setVisibility(View.INVISIBLE);
        d_txt.setVisibility(View.INVISIBLE);
        btnA.setVisibility(View.INVISIBLE);
        btnB.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        btnC.setVisibility(View.INVISIBLE);
        btnD.setVisibility(View.INVISIBLE);
        multi_img1.setVisibility(View.INVISIBLE);
        multi_img2.setVisibility(View.INVISIBLE);
        multi_img3.setVisibility(View.INVISIBLE);
        multi_img4.setVisibility(View.INVISIBLE);
    }

    private void chooesQuestion() {
        txtA.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        txtB.setVisibility(View.VISIBLE);
        txtC.setVisibility(View.VISIBLE);
        txtD.setVisibility(View.VISIBLE);
        a_txt.setVisibility(View.VISIBLE);
        b_txt.setVisibility(View.VISIBLE);
        c_txt.setVisibility(View.VISIBLE);
        d_txt.setVisibility(View.VISIBLE);
        btnA.setVisibility(View.VISIBLE);

        btnA.setOnClickListener(view -> {
            btnA.setBackgroundResource(R.drawable.multi_reply_oval_blue);
            txtA.setTextColor(Color.WHITE);
            answers.put("additionalProp1", a);
        });
        btnB.setVisibility(View.VISIBLE);
        btnB.setOnClickListener(view -> {
            btnB.setBackgroundResource(R.drawable.multi_reply_oval_blue);
            txtB.setTextColor(Color.WHITE);
            answers.put("additionalProp1", b);
        });
        btnC.setVisibility(View.VISIBLE);
        btnC.setOnClickListener(view -> {
            btnC.setBackgroundResource(R.drawable.multi_reply_oval_blue);
            txtC.setTextColor(Color.WHITE);
            answers.put("additionalProp1", c);
        });
        btnD.setVisibility(View.VISIBLE);
        btnD.setOnClickListener(view -> {
            btnD.setBackgroundResource(R.drawable.multi_reply_oval_blue);
            txtD.setTextColor(Color.WHITE);
            answers.put("additionalProp1", d);
        });
        multi_img1.setVisibility(View.VISIBLE);
        multi_img2.setVisibility(View.VISIBLE);
        multi_img3.setVisibility(View.VISIBLE);
        multi_img4.setVisibility(View.VISIBLE);
        replyEdit.setVisibility(View.INVISIBLE);
        reply_X.setVisibility(View.INVISIBLE);
        reply_O.setVisibility(View.INVISIBLE);
        ox_bar.setVisibility(View.INVISIBLE);
    }

    private void oxQuestion() {
        reply_X.setVisibility(View.VISIBLE);
        reply_O.setVisibility(View.VISIBLE);
        reply_X.setOnClickListener(view -> {
            reply_X.setImageResource(R.drawable.btn_x_sel);
            answers.put("additionalProp1", "X");
        });
        reply_O.setOnClickListener(view -> {
            reply_O.setImageResource(R.drawable.group_3_blue);
            answers.put("additionalProp1", "O");
        });
        scrollView.setVisibility(View.INVISIBLE);
        ox_bar.setVisibility(View.VISIBLE);
        replyEdit.setVisibility(View.INVISIBLE);
        txtA.setVisibility(View.INVISIBLE);
        txtB.setVisibility(View.INVISIBLE);
        txtC.setVisibility(View.INVISIBLE);
        txtD.setVisibility(View.INVISIBLE);
        a_txt.setVisibility(View.INVISIBLE);
        b_txt.setVisibility(View.INVISIBLE);
        c_txt.setVisibility(View.INVISIBLE);
        d_txt.setVisibility(View.INVISIBLE);
        btnA.setVisibility(View.INVISIBLE);
        btnB.setVisibility(View.INVISIBLE);
        btnC.setVisibility(View.INVISIBLE);
        btnD.setVisibility(View.INVISIBLE);
        multi_img1.setVisibility(View.INVISIBLE);
        multi_img2.setVisibility(View.INVISIBLE);
        multi_img3.setVisibility(View.INVISIBLE);
        multi_img4.setVisibility(View.INVISIBLE);
    }
}