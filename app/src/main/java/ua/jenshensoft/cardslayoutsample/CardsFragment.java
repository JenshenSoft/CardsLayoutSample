package ua.jenshensoft.cardslayoutsample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.views.CardView;
import ua.jenshensoft.cardslayout.views.CardsLayout;

/**
 * Created by Евгений on 03.12.2015.
 */
public class CardsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View rootView  = inflater.inflate(R.layout.content_main, container, false);


        final CardsLayout cardslayout = (CardsLayout) rootView.findViewById(R.id.cardslayout);
        ImageView card0 = (ImageView) rootView.findViewById(R.id.card_imageView_0);
        card0.setImageResource(R.drawable.cat);
        ImageView card1 = (ImageView) rootView.findViewById(R.id.card_imageView_1);
        card1.setImageResource(R.drawable.cat);
        ImageView card2 = (ImageView) rootView.findViewById(R.id.card_imageView_2);
        card2.setImageResource(R.drawable.cat);
        ImageView card3 = (ImageView) rootView.findViewById(R.id.card_imageView_3);
        card3.setImageResource(R.drawable.cat);
        ImageView card4 = (ImageView) rootView.findViewById(R.id.card_imageView_4);
        card4.setImageResource(R.drawable.cat);
        ImageView card5 = (ImageView) rootView.findViewById(R.id.card_imageView_5);
        card5.setImageResource(R.drawable.cat);
        ImageView card6 = (ImageView) rootView.findViewById(R.id.card_imageView_6);
        card6.setImageResource(R.drawable.cat);

        cardslayout.setCardTranslationListener(new OnCardTranslationListener() {
            @Override
            public void onCardTranslation(float positionX, float positionY, CardInfo cardInfo, boolean isTouched) {
                if (!isTouched) {
                    cardslayout.removeCardView(cardInfo.getCardIndex());
                    cardslayout.setCardViewsState(true, cardInfo.getCardIndex());
                } else {
                    cardslayout.setCardViewsState(false, cardInfo.getCardIndex());
                }
            }
        });
        return rootView;
    }
}
