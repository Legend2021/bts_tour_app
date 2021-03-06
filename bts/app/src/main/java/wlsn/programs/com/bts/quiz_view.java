package wlsn.programs.com.bts;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Leon on 8/4/18.
 */

public class quiz_view extends Fragment {

    FirebaseAuth mAuth;
    FirebaseDatabase fDb;
    DatabaseReference user_quiz;
    DatabaseReference user_stats;

    InputMethodManager imm;
    user current_user;
    View multi_choice;
    TextView mc_question;
    Button mc_choice_1;
    Button mc_choice_2;
    Button mc_choice_3;
    Button mc_choice_4;

    //MULTIPLE ANSWER
    View multi_answer;
    TextView ma_question;
    Button ma_choice_1;
    Button ma_choice_2;
    Button ma_choice_3;
    Button ma_choice_4;
    Button ma_check_button;

    boolean[] selected = new boolean[4];


    //SHORT ANSWER
    View short_ans;
    TextView sa_question;
    EditText sa_answer;
    Button sa_submit_button;

    //RESULTS
    View re;
    TextView re_name;
    TextView re_building;
    TextView re_ratio;
    ProgressBar re_progress;
    Button re_exit;
    ExpandableListView re_questions;

    int current_question = 0;
    int current_score = 0;
    quiz current_quiz;
    public static quiz_view getInstance(int quiz)
    {
        quiz_view single_quiz = new quiz_view();
        Bundle args = new Bundle();
        args.putInt("quiz",quiz);
        single_quiz.setArguments(args);
        return single_quiz;
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view = inflater.inflate(R.layout.quiz_view, container, false);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        current_user = user.getInstance();

        mAuth = FirebaseAuth.getInstance();
        String user_key = mAuth.getCurrentUser().getUid().toString();
        fDb = FirebaseDatabase.getInstance();
        imm = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);



        //QUESTION LAYOUT
        multi_choice = view.findViewById(R.id.multiple_choice_layout);
        mc_question = multi_choice.findViewById(R.id.mc_text);
        mc_choice_1 = multi_choice.findViewById(R.id.mc_choice_1);
        mc_choice_2 = multi_choice.findViewById(R.id.mc_choice_2);
        mc_choice_3 = multi_choice.findViewById(R.id.mc_choice_3);
        mc_choice_4 = multi_choice.findViewById(R.id.mc_choice_4);
        multi_choice.setVisibility(View.GONE);

        multi_answer = view.findViewById(R.id.multiple_select_layout);
        ma_question = multi_answer.findViewById(R.id.ms_text);
        ma_choice_1 = (Button) multi_answer.findViewById(R.id.ms_choice_1);
        ma_choice_2 = (Button) multi_answer.findViewById(R.id.ms_choice_2);
        ma_choice_3 = (Button) multi_answer.findViewById(R.id.ms_choice_3);
        ma_choice_4 = (Button) multi_answer.findViewById(R.id.ms_choice_4);
        ma_check_button = (Button) multi_answer.findViewById((R.id.ms_submit));
        multi_answer.setVisibility(View.GONE);


        short_ans = view.findViewById(R.id.short_answer_layout);
        sa_question = short_ans.findViewById(R.id.sa_text);
        sa_answer = (EditText) short_ans.findViewById(R.id.sa_answer_field);
        sa_submit_button = (Button) short_ans.findViewById(R.id.sa_submit);
        short_ans.setVisibility(View.GONE);


        sa_answer.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    //sa_answer.setInputType(InputType.TYPE_NULL);
                    return true;
                }
                return false;
            }
        });


        re = view.findViewById(R.id.results_layout);
        re_building = (TextView) re.findViewById(R.id.result_building);
        re_name = (TextView) re.findViewById(R.id.result_name);
        re_progress = (ProgressBar) re.findViewById(R.id.result_progress);
        re_ratio = (TextView) re.findViewById(R.id.result_ratio);
        re_questions = (ExpandableListView) re.findViewById(R.id.result_questions);
        re_exit = (Button) re.findViewById(R.id.result_button);
        re.setVisibility(View.GONE);

        Bundle bundle = getArguments();
        int selected_quiz = bundle.getInt("quiz");
        current_quiz = current_user.getData().getQuizzes()[selected_quiz];

        user_quiz = fDb.getReference("users/" + user_key+"/quizzes/" + current_quiz.getName());
        user_stats = fDb.getReference("users/" + user_key+"/progress/");
        getActivity().setTitle(current_quiz.getName());
        startQuiz();

        mc_choice_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(current_quiz.getQuestions()[current_question].getAnswers()[0].isCorrect())
                {
                    if(!current_quiz.getQuestions()[current_question].alreadyCorrect())
                    {
                        current_quiz.total_correct += 1;
                        current_quiz.getQuestions()[current_question].setAlreadyCorrect(true);
                        user_quiz.child("questions").child("question_" +String.valueOf(current_question + 1)).child("already_correct").setValue(true);

                        Snackbar.make(view, "Congratulations! That's a new correct answer!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                    {
                        Snackbar.make(view, "Great memory! Keep it up!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    current_score += 1;
                }
                else
                {
                    Snackbar.make(view, "Sorry, that was incorrect.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                if(current_question < current_quiz.getQuestions().length - 1)
                {
                    current_question += 1;
                    nextQuestion(current_question, current_quiz);
                }
                else
                {
                    results();
                }
            }
        });

        mc_choice_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current_quiz.getQuestions()[current_question].getAnswers()[1].isCorrect())
                {
                    if(!current_quiz.getQuestions()[current_question].alreadyCorrect())
                    {
                        current_quiz.total_correct += 1;
                        current_quiz.getQuestions()[current_question].setAlreadyCorrect(true);
                        user_quiz.child("questions").child("question_" +String.valueOf(current_question + 1)).child("already_correct").setValue(current_quiz.getQuestions()[current_question].alreadyCorrect());

                        Snackbar.make(view, "Congratulations! That's a new correct answer!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                    {
                        Snackbar.make(view, "Great memory! Keep it up!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    current_score += 1;
                }
                else
                {
                    Snackbar.make(view, "Sorry, that was incorrect.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                if(current_question < current_quiz.getQuestions().length - 1)
                {
                    current_question += 1;
                    nextQuestion(current_question, current_quiz);
                }
                else
                {
                    results();
                }
            }
        });

        mc_choice_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current_quiz.getQuestions()[current_question].getAnswers()[2].isCorrect())
                {
                    if(!current_quiz.getQuestions()[current_question].alreadyCorrect())
                    {
                        current_quiz.total_correct += 1;
                        current_quiz.getQuestions()[current_question].setAlreadyCorrect(true);
                        user_quiz.child("questions").child("question_" +String.valueOf(current_question + 1)).child("already_correct").setValue(true);

                        Snackbar.make(view, "Congratulations! That's a new correct answer!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                    {
                        Snackbar.make(view, "Great memory! Keep it up!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    current_score += 1;
                }
                else
                {
                    Snackbar.make(view, "Sorry, that was incorrect.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                if(current_question < current_quiz.getQuestions().length - 1)
                {
                    current_question += 1;
                    nextQuestion(current_question, current_quiz);
                }
                else
                {
                    results();
                }
            }
        });

        mc_choice_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current_quiz.getQuestions()[current_question].getAnswers()[3].isCorrect())
                {
                    if(!current_quiz.getQuestions()[current_question].alreadyCorrect())
                    {
                        current_quiz.total_correct += 1;
                        current_quiz.getQuestions()[current_question].setAlreadyCorrect(true);
                        user_quiz.child("questions").child("question_" +String.valueOf(current_question + 1)).child("already_correct").setValue(current_quiz.getQuestions()[current_question].alreadyCorrect());

                        Snackbar.make(view, "Congratulations! That's a new correct answer!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                    {
                        Snackbar.make(view, "Great memory! Keep it up!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    current_score += 1;
                }
                else
                {
                    Snackbar.make(view, "Sorry, that was incorrect.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                if(current_question < current_quiz.getQuestions().length - 1)
                {
                    current_question += 1;
                    nextQuestion(current_question,current_quiz);
                }
                else
                {
                    results();
                }
            }
        });


        //MULTIPLE ANSWER
        ma_choice_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected[0])
                {
                    selected[0] = false;
                    ma_choice_1.setBackgroundResource(R.drawable.button_unselected);

                }
                else
                {
                    ma_choice_1.setBackgroundResource(R.drawable.button_selected);
                    selected[0] = true;
                }

            }
        });

        ma_choice_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected[1])
                {
                    selected[1] = false;
                    ma_choice_2.setBackgroundResource(R.drawable.button_unselected);
                }
                else
                {
                    selected[1] = true;
                    ma_choice_2.setBackgroundResource(R.drawable.button_selected);

                }

            }
        });

        ma_choice_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected[2])
                {
                    selected[2] = false;
                    ma_choice_3.setBackgroundResource(R.drawable.button_unselected);
                }
                else
                {
                    selected[2] = true;
                    ma_choice_3.setBackgroundResource(R.drawable.button_selected);

                }

            }
        });

        ma_choice_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected[3])
                {
                    selected[3] = false;
                    ma_choice_4.setBackgroundResource(R.drawable.button_unselected);
                }
                else
                {
                    selected[3] = true;
                    ma_choice_4.setBackgroundResource(R.drawable.button_selected);
                }

            }
        });

        ma_check_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean correct = true;
                for(int i = 0; i < selected.length;i++)
                {
                    //SOMETHING STRANGE IS HAPPENING HERE ON THE FINAL ITERATION
                    if((selected[i] ^ current_quiz.getQuestions()[current_question].getAnswers()[i].isCorrect()))
                    {
                        correct = false;
                    }
                }

                if(correct)
                {
                    if(!current_quiz.getQuestions()[current_question].alreadyCorrect())
                    {
                        current_quiz.total_correct += 1;
                        current_quiz.getQuestions()[current_question].setAlreadyCorrect(true);
                        user_quiz.child("questions").child("question_" +String.valueOf(current_question + 1)).child("already_correct").setValue(current_quiz.getQuestions()[current_question].alreadyCorrect());

                        Snackbar.make(view, "Congratulations! That's a new correct answer!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                    {
                        Snackbar.make(view, "Great memory! Keep it up!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    current_score += 1;
                }
                else
                {
                    Snackbar.make(view, "Sorry, that was incorrect.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


                ma_choice_1.setBackgroundResource(R.drawable.button_unselected);
                ma_choice_2.setBackgroundResource(R.drawable.button_unselected);
                ma_choice_3.setBackgroundResource(R.drawable.button_unselected);
                ma_choice_4.setBackgroundResource(R.drawable.button_unselected);
                if(current_question < current_quiz.getQuestions().length - 1)
                {
                    for(int i = 0; i < selected.length; i++)
                    {
                        selected[i] = false;
                    }
                    current_question += 1;
                    nextQuestion(current_question,current_quiz);
                }
                else
                {
                    results();
                }
            }
        });

        sa_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO : Update DB
                user_quiz.child("questions").child("question_" +String.valueOf(current_question + 1)).child("answers").child("answer").child("text").setValue(sa_answer.getText().toString());
                Snackbar.make(view, "Your answer has been submitted!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(current_question < current_quiz.getQuestions().length - 1)
                {
                    current_question += 1;
                    nextQuestion(current_question,current_quiz);
                }
                else
                {
                    results();
                }
            }
        });

        re_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new frag_main();
                if(fragment != null)
                {
                    FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
                    trans.replace(R.id.content_frame,fragment);
                    trans.addToBackStack(null);
                    trans.commit();
                }
            }
        });
    }

    private void startQuiz()
    {
        //LW_BUG_7: System doesn't seem to recognize change between multiple choice and multiple select
        multi_choice.setVisibility(View.GONE);
        multi_answer.setVisibility(View.GONE);
        short_ans.setVisibility(View.GONE);

        switch(current_quiz.getQuestions()[current_question].getType())
        {
            case multiple_choice:
                //SET VISIBILITY
                multi_choice.setVisibility(View.VISIBLE);

                //SET QUESTION TEXT
                mc_question.setText(current_quiz.getQuestions()[current_question].getQuestionText());

                //SET ANSWER TEXT
                mc_choice_1.setText(current_quiz.getQuestions()[current_question].getAnswers()[0].getAnswerText());
                mc_choice_2.setText(current_quiz.getQuestions()[current_question].getAnswers()[1].getAnswerText());
                mc_choice_3.setText(current_quiz.getQuestions()[current_question].getAnswers()[2].getAnswerText());
                mc_choice_4.setText(current_quiz.getQuestions()[current_question].getAnswers()[3].getAnswerText());

                //CHECK ANSWER
                break;
            case multiple_answer:
                multi_answer.setVisibility(View.VISIBLE);

                //SET QUESTION TEXT
                ma_question.setText(current_quiz.getQuestions()[current_question].getQuestionText());

                //SET ANSWER TEXT
                ma_choice_1.setText(current_quiz.getQuestions()[current_question].getAnswers()[0].getAnswerText());
                ma_choice_2.setText(current_quiz.getQuestions()[current_question].getAnswers()[1].getAnswerText());
                ma_choice_3.setText(current_quiz.getQuestions()[current_question].getAnswers()[2].getAnswerText());
                ma_choice_4.setText(current_quiz.getQuestions()[current_question].getAnswers()[3].getAnswerText());
                break;
            case short_answer:
                short_ans.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void nextQuestion(int current_, quiz quiz_)
    {
        //remember to increment before calling function

        //HIDE All layouts
        multi_choice.setVisibility(View.GONE);
        multi_answer.setVisibility(View.GONE);
        short_ans.setVisibility(View.GONE);

        //quiz_.getQuestions()[current_ + 1].
        switch(quiz_.getQuestions()[current_].getType())
        {
            case multiple_choice:
                //SET QUESTION TEXT
                multi_choice.setVisibility(View.VISIBLE);
                mc_question.setText(quiz_.getQuestions()[current_].getQuestionText());

                //SET ANSWER
                mc_choice_1.setText(quiz_.getQuestions()[current_].getAnswers()[0].getAnswerText());
                mc_choice_2.setText(quiz_.getQuestions()[current_].getAnswers()[1].getAnswerText());
                mc_choice_3.setText(quiz_.getQuestions()[current_].getAnswers()[2].getAnswerText());
                mc_choice_4.setText(quiz_.getQuestions()[current_].getAnswers()[3].getAnswerText());
                break;
            case multiple_answer:
                multi_answer.setVisibility(View.VISIBLE);
                //SET QUESTION TEXT
                ma_question.setText(current_quiz.getQuestions()[current_question].getQuestionText());

                //SET ANSWER TEXT
                ma_choice_1.setText(current_quiz.getQuestions()[current_question].getAnswers()[0].getAnswerText());
                ma_choice_2.setText(current_quiz.getQuestions()[current_question].getAnswers()[1].getAnswerText());
                ma_choice_3.setText(current_quiz.getQuestions()[current_question].getAnswers()[2].getAnswerText());
                ma_choice_4.setText(current_quiz.getQuestions()[current_question].getAnswers()[3].getAnswerText());
                break;
            case short_answer:
                short_ans.setVisibility(View.VISIBLE);
                sa_question.setText(current_quiz.getQuestions()[current_question].getQuestionText());
                break;
        }
    }

    public void results()
    {
        multi_choice.setVisibility(View.GONE);
        multi_answer.setVisibility(View.GONE);
        short_ans.setVisibility(View.GONE);
        re.setVisibility(View.VISIBLE);

        //TODO: Create Results processing function
        current_user.getStats().current_points += current_quiz.total_correct - current_quiz.previous_total;
        current_quiz.previous_total = current_quiz.total_correct;

        re_name.setText(current_quiz.getName());
        re_building.setText("Building " + current_quiz.building_num + ":");
        re_ratio.setText("You've answered " + current_quiz.total_correct + " out of " + current_quiz.total_questions + " correctly.");
        re_progress.setProgress(current_quiz.total_correct);
        re_progress.setMax(current_quiz.total_questions);

        if(current_user.getStats().LEVELUP())
        {
            user_stats.child("current_level").setValue(current_user.getStats().current_level);
            user_stats.child("level_name").setValue(current_user.getStats().level_name);
            user_stats.child("points_until_levelup").setValue(current_user.getStats().points_until_levelup);
        }
        user_stats.child("current_points").setValue(current_user.getStats().current_points);

        if(current_quiz.quizComplete())
        {
            //user_quiz.child("is_complete").setValue(true);
        }
        user_quiz.child("previous_total").setValue(current_quiz.previous_total);
        user_quiz.child("total_correct").setValue(current_quiz.total_correct);
        //onBackPressed();
    }
}
