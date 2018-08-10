package project3;

import java.util.Arrays;
import java.util.Comparator;
import javafx.scene.paint.Color;

public class WoWUtilities {

    ////////////////////////////////
    // HELPER METHODS
    ///////////////////////////////////
    
    // Retrieve the color of the player in question
    // In the World of Warcraft world this represents their class color (ex: Druids are orange, Warriors are brown, etc.)
    public static Color getRaiderColor(String raiderName) {

        Color classColor = Color.color(1.0f, 1.0f, 1.0f);

        switch (raiderName) {
            // Demon Hunter
            case "Sylphilis-Stormreaver":
            case "Snozz-Stormreaver":
                classColor = Color.color(0.64f, 0.19f, 0.79f);
                break;
            // DK
            case "Alezz-Stormreaver":
                classColor = Color.color(0.77f, 0.12f, 0.23f);
                break;
            // Druid
            case "Acrylate-Stormreaver":
            case "Infinitum-Stormreaver":
            case "Kickproof-Stormreaver":
            case "Nikkelz-Stormreaver":
                classColor = Color.color(1.0f, 0.49f, 0.04f);
                break;
            // Hunter
            case "Hello-Stormreaver":
            case "Kkyle-Stormreaver":
                classColor = Color.color(0.67f, 0.83f, 0.45f);
                break;
            // Mage
            case "Clovuchul-Stormreaver":
            case "Sovm-Stormreaver":
            case "Spasticmage-Stormreaver":
                classColor = Color.color(0.41f, 0.80f, 0.94f);
                break;
            // Paladin
            case "Bristerpal-Stormreaver":
            case "Kurt-Stormreaver":
            case "Shadaka-Stormreaver":
                classColor = Color.color(0.96f, 0.55f, 0.73f);
                break;
            // Priest
            case "Chebag-Stormreaver":
            case "Mvrick-Stormreaver":
                classColor = Color.color(0.95f, 0.95f, 0.95f); // off-white
                break;
            // Rogue
            case "Galatapus-Stormreaver":
                classColor = Color.color(1.00f, 0.96f, 0.41f); // yellow
                break;
            // Shaman
            case "Aerievore-Stormreaver":
            case "Hogsham-Stormreaver":
            case "Zody-Stormreaver":
                classColor = Color.color(0.00f, 0.44f, 0.87f); // blue
                break;
            // Warlock
            case "Edgothar-Stormreaver":
                classColor = Color.color(0.58f, 0.51f, 0.79f); // purple
                break;
            // Warrior
            case "Tentonaxe-Stormreaver":
                classColor = Color.color(0.78f, 0.61f, 0.43f); // brown
                break;

        }

        return classColor;

    }

    // This will let you know what kind of event you are looking for - a healing or damage one (or one you can ignore)
    public static int getEventType(String logEventName) {

        // eventType - We will use the following:
        // 0 - Unknown
        // 1 - Damage
        // 2 - Heal
        int eventType = 0;

        /* Known identifiers (see the web page listed above)
         * Damage
         * SPELL_PERIODIC_DAMAGE
         * SWING_DAMAGE
         * RANGE_DAMAGE
         * SPELL_DAMAGE
         * 
         * Heal
         * SPELL_HEAL
         * SPELL_PERIODIC_HEAL
         * SPELL_ABSORBED
         */
        if (logEventName.equals("SPELL_DAMAGE")
                || logEventName.equals("SPELL_PERIODIC_DAMAGE")
                || logEventName.equals("SWING_DAMAGE_LANDED")
                || logEventName.equals("RANGE_DAMAGE")
                || logEventName.equals("DAMAGE_SPLIT")) {
            eventType = 1;
        } else if (logEventName.equals("SPELL_HEAL")
                || logEventName.equals("SPELL_PERIODIC_HEAL")
                || logEventName.equals("SPELL_ABSORBED")) {
            eventType = 2;
        } else {
            eventType = 0;
        }

        return eventType;

    }

    // This will sort lists for Damage and Healing  
    public static void sortWowPlayers(String sortByType, WoWPlayer[] pList) {

        // sort array by name
        if (sortByType.equals("damage")) {
            Arrays.sort(pList, new SortDamage());
            // Arrays.Sort( playerList, delegate(WoWPlayer x, WoWPlayer y) { return y.DamageCount.CompareTo(x.DamageCount); });
        } else {
           Arrays.sort(pList, new SortHealing());
        }

    }

}

// Compare Methods
// They are called from sortWowPlayers to do a custom compare (in this case comparing damage/healing of one player to another) and helps
// order them from highest->lowest
class SortHealing implements Comparator<WoWPlayer> {
    @Override
    public int compare(WoWPlayer a, WoWPlayer b) {
        
        if(a.getHealCount() < b.getHealCount()){
            return 1;
        }
        else
            return -1;
    }
}

class SortDamage implements Comparator<WoWPlayer> {
    @Override
    public int compare(WoWPlayer a, WoWPlayer b) {
        
        if(a.getDamageCount() < b.getDamageCount()){
            return 1;
        }
        else
            return -1;
    }
}
