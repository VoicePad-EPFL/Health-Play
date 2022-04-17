package ch.epfl.sdp.healthplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ch.epfl.sdp.healthplay.database.Database;

public class LeaderBoardActivity extends AppCompatActivity {

    private Database db = new Database();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView[] tab  = new TextView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        ArrayList<String> l = new ArrayList<>();
        l.add("a");
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        map.put("80",l);
        HashMap<String,HashMap<String, ArrayList<String>>> leaderBoard = new HashMap<>();
        leaderBoard.put(Database.getTodayDate(), map);
        db.mDatabase.child(Database.LEADERBOARD).setValue(leaderBoard);
        if(mAuth.getCurrentUser() != null) {
            db.addHealthPoint(mAuth.getCurrentUser().getUid(), 40);
            tab[0] = findViewById(R.id.top1);
            tab[1] = findViewById(R.id.top2);
            tab[2] = findViewById(R.id.top3);
            tab[3] = findViewById(R.id.top4);
            tab[4] = findViewById(R.id.top5);
            initTop5();

        }
    }

    private void initTop5() {

        db.mDatabase.child(Database.LEADERBOARD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                @SuppressWarnings("unchecked")
                HashMap<String,HashMap<String, ArrayList<String>>> leaderBoard = (HashMap<String,HashMap<String, ArrayList<String>>>) snapshot.getValue();
                if(leaderBoard != null && leaderBoard.containsKey(Database.getTodayDate())) {
                    TreeMap<String, ArrayList<String>> leaderBoardOrdered = new TreeMap<>(Database.comparator);
                    leaderBoardOrdered.putAll(leaderBoard.get(Database.getTodayDate()));
                    int count = 0;
                    int myTop = 0;
                    for (TreeMap.Entry<String, ArrayList<String>> entry : leaderBoardOrdered.entrySet()) {
                        String hp = entry.getKey();
                        for(String e: entry.getValue()) {
                            int top = count + 1;
                            if(e.equals(mAuth.getUid())) {
                                myTop = top;
                            }
                            if (count < 5){
                                tab[count].setText(top + " / " + e + " / " + hp);
                            }
                            count++;
                        }
                    }
                    while(count < 5) {
                            tab[count].setText("No user");
                            count++;
                    }

                    TextView myTopText = findViewById(R.id.myTop);
                    if(myTop > 0) {
                        if(myTop > 5) {
                            myTopText.setText("(you are currently rank " + myTop + ")");
                        }
                        else {
                            myTopText.setText("(Congrats you made it to the top 5)");
                        }
                    }
                    else {
                        myTopText.setText("(you are currently unranked)");
                    }
                }
                else {
                    int count = 0;
                    while(count < 5) {
                        tab[count].setText("No user");
                        count++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("firebase", "Error:onCancelled", error.toException());
            }
        });

    }


}