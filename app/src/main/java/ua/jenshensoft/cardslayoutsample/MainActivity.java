package ua.jenshensoft.cardslayoutsample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.util.List;

import ua.jenshensoft.cardslayout.views.CardView;
import ua.jenshensoft.cardslayout.views.CardsLayout;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CardsLayout cardslayout = (CardsLayout) findViewById(R.id.cardslayout);
        ImageView card0 = (ImageView) findViewById(R.id.card_imageView_0);
        card0.setImageResource(R.drawable.cat);
        ImageView card1 = (ImageView) findViewById(R.id.card_imageView_1);
        card1.setImageResource(R.drawable.cat);
        ImageView card2 = (ImageView) findViewById(R.id.card_imageView_2);
        card2.setImageResource(R.drawable.cat);
        ImageView card3 = (ImageView) findViewById(R.id.card_imageView_3);
        card3.setImageResource(R.drawable.cat);
        ImageView card4 = (ImageView) findViewById(R.id.card_imageView_4);
        card4.setImageResource(R.drawable.cat);
        ImageView card5 = (ImageView) findViewById(R.id.card_imageView_5);
        card5.setImageResource(R.drawable.cat);
        ImageView card6 = (ImageView) findViewById(R.id.card_imageView_6);
        card6.setImageResource(R.drawable.cat);
        cardslayout.setConfigurationForCard(new CardsLayout.OnConfigureCard() {
            @Override
            public void onConfiguration(CardView card) {
            }
        });

        cardslayout.setConfigurationForList(new CardsLayout.OnConfigureList() {
            @Override
            public void onConfiguration(List<CardView> cards) {
            }
        });
    }
}
