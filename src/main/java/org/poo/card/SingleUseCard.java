package org.poo.card;

import org.poo.utils.Utils;

public class SingleUseCard extends Card {

    public SingleUseCard(final String accountIban, final String email) {
        super(accountIban, email);
    }

    /***
     * Returns true if the card is single use
     * @return boolean
     */
    @Override
    public boolean isOneTime() {
        return true;
    }

    /***
     * Regenerates the card number if the card is single use
     * @return String
     */
    @Override
    public String  regenerateCardNumber() {
        super.setCardNumber(Utils.generateCardNumber());
        return getCardNumber();
    }
}
