package fs.sdvbs.bts_tour;

/**
 * Created by Leon on 6/26/18.
 */

public class short_answer implements question {

    question_types question_type = question_types.multiple_choice;

    String question_text = "";
    answer[] answer_pool;

    int building_num = -1;
    boolean is_correct = false;
    boolean already_answered_correctly = false;

    @Override
    public question_types getType() {
        return question_type;
    }

    @Override
    public String getQuestionText() {
        return question_text;
    }

    @Override
    public boolean isCorrect() {
        return is_correct;
    }

    @Override
    public void setAlreadyCorrect(boolean bool_) {

    }

    @Override
    public boolean alreadyCorrect() {
        return already_answered_correctly;
    }

    @Override
    public answer[] getAnswers() {
        return new answer[0];
    }

    @Override
    public answer getAnswer() {
        return null;
    }
}
