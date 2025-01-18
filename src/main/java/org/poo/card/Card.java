package org.poo.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

@Getter
@Setter
public class Card {
    private String cardNumber;
    private String email;
    private final String accountIban;
    private String status;

    public Card(final String accountIban, final String email) {
        this.email = email;
        this.accountIban = accountIban;
        status = "active";
        cardNumber = Utils.generateCardNumber();
    }

    /***
     * Outputs the card to JSON format
     * @param cardArray the ArrayNode to which the output is written
     */
    public void print(final ArrayNode cardArray) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode card = objectMapper.createObjectNode();
        card.put("cardNumber", cardNumber);
        card.put("status", status);
        cardArray.add(card);
    }

    /***
     * Is overridden SingleUseCard to regenerate the card number
     * @return String
     */
    public String regenerateCardNumber() {
        return null;
    }

    /***
     * Returns false if card is not single use
     * @return boolean
     */
    public boolean isOneTime() {
        return false;
    }
}
