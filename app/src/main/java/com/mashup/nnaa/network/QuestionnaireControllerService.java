package com.mashup.nnaa.network;

import com.mashup.nnaa.network.model.Questionnaire;
import com.mashup.nnaa.network.model.QuestionnaireAnswerDto;
import com.mashup.nnaa.network.model.QuestionnaireDto;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface QuestionnaireControllerService {

    // 질문지 보내기
    @POST("questionnaire")
    Call<Questionnaire> postQuestionnaire(@Body HashMap<String, String> body);

    // 질문지 보기
    @GET("questionnaire/{questionnaireId}")
    Call<QuestionnaireDto> getQuestionnaire(@Path("questionnaireId") String questionnaireId);

    // 질문지에 답변하기
    @PUT("questionnaire/{questionnaireId")
    Call<Questionnaire> answerQuestionnaire(@Path("questionnaireId") String questionnaireId,
                                            QuestionnaireAnswerDto questionnaireAnswerDto);
    // 받은 질문지 리스트 보기
    @GET("questionnaire/inbox")
    Call<List<QuestionnaireDto>> getReceiveQuestionnaires();

    // 보낸 질문지 리스트 보기
    @GET("questionnaire/outbox")
    Call<List<QuestionnaireDto>> getSendQuestionnaires();

}
