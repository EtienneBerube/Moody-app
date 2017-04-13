package ultramirinc.champs_mood;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ultramirinc.champs_mood.fragments.MyAdapter;
import ultramirinc.champs_mood.fragments.Person;
import ultramirinc.champs_mood.fragments.TimePicker;

public class ScheduleAdder extends AppCompatActivity implements DialogInterface.OnDismissListener{

    private Context context = this;
    private List<Break> breakList= new ArrayList<>();
    private RecyclerView recyclerView;

    public ScheduleAdder(){
        populateList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_adder);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapterSchedule(breakList));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Breaks");

        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BreakCreator mDialog = new BreakCreator();
                mDialog.show(getSupportFragmentManager(), "start break Creator");

            }
        });
    }

    public void populateList(){
        //TODO get shit from database
        breakList.add(new Break(new Time(1, 30), new Time(2, 00), "Monday"));
        breakList.add(new Break(new Time(1, 30), new Time(2, 00), "Monday"));
        breakList.add(new Break(new Time(1, 30), new Time(2, 00), "Tuesday"));
        breakList.add(new Break(new Time(1, 30), new Time(2, 00), "Wednesday"));
        breakList.add(new Break(new Time(1, 30), new Time(2, 00), "Wednesday"));
        breakList.add(new Break(new Time(1, 30), new Time(2, 00), "Friday"));
    }
    //Change implements OnBreakReady
    /*
    @Override
    public void onBreakReadyListener(String text) {
        String[] temp = text.split(":");
        String day = temp[0];
        int startMinute = Integer.parseInt(temp[1]);
        int startHour = Integer.parseInt(temp[2]);
        int endMinute = Integer.parseInt(temp[3]);
        int endHour = Integer.parseInt(temp[4]);
        Break mBreak = new Break(new ultramirinc.champs_mood.Time(startHour, startMinute),
                new ultramirinc.champs_mood.Time(endHour, endMinute), day);
    }
    */

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(dialog instanceof BreakCreator){
            BreakCreator mBreakCreator = (BreakCreator) dialog;

            if(!mBreakCreator.isCancelled()){
                int startMinute = mBreakCreator.getStartMinute();
                int startHour = mBreakCreator.getStartHour();
                int endMinute = mBreakCreator.getEndMinute();
                int endHour = mBreakCreator.getEndHour();
                String day = mBreakCreator.getDay();

                Break mBreak = new Break(new ultramirinc.champs_mood.Time(startHour, startMinute),
                        new ultramirinc.champs_mood.Time(endHour, endMinute), day);
                breakList.add(mBreak);
                //getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
            }
        }
    }

    
}
