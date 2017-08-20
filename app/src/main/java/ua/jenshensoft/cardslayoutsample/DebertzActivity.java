package ua.jenshensoft.cardslayoutsample;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.table.OnCardClickListener;
import ua.jenshensoft.cardslayoutsample.views.debertz.DebertzGameTableLayout;

public class DebertzActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debertz);
        final DebertzGameTableLayout tableLayout = findViewById(R.id.debertzTable);
        tableLayout.setOnCardClickListener(new OnCardClickListener() {
            @Override
            public void onCardAction(CardInfo cardInfo) {
                cardInfo.toString();
            }
        });
    }
}
