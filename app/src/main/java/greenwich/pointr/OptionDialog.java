package greenwich.pointr;


import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class OptionDialog extends DialogFragment{
    String[] typesList = {"Place1", "Test"}; // put some random values just for testing purposes
    ArrayList<Integer> mSelectedItems;
    String[] typeSelected;
    public void showOption(final Context context, final String name, final ToggleButton toggleButton, boolean isChecked){
        mSelectedItems = new ArrayList();  // Where we track the selected items
        typesList = getType(name);
        if (savedSelected(name, context, typesList.length) != null && isChecked){
            mSelectedItems = savedSelected(name, context, typesList.length);
        }
        else {
            mSelectedItems = defaultValues(typesList.length, isChecked);
        }
        boolean[] checkBoxes;
        checkBoxes = selectBoxes(mSelectedItems, typesList.length);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //boolean[] test = {true, true};
        // Set the dialog title
        builder.setTitle("Refine your search")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(typesList, checkBoxes,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int[] boolSelected = new int[mSelectedItems.size()];
                        typeSelected = new String[mSelectedItems.size()];
                        for (int i = 0; i < mSelectedItems.size(); i++){
                            System.out.println(typesList[mSelectedItems.get(i)] + " " + mSelectedItems.get(i));
                            boolSelected[i] = mSelectedItems.get(i);
                            typeSelected[i] = typesList[mSelectedItems.get(i)];
                        }
                        SavePreferences(context, typeSelected, name, boolSelected);
                        if (mSelectedItems.size() > 0){
                            toggleButton.setChecked(true);
                        }
                        else {
                            toggleButton.setChecked(false);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).show();

    }

    String[] getType(String name){
        String[] typesList = {};
            if (name == "Animals") {
                typesList = new String []{"Veterinary care", "Zoo"};
            } else if (name == "Accomodation") {
                typesList = new String[] {"Lodging"};
            } else if (name == "Food & Drink") {
                typesList = new String[] {"Bakery", "Bar", "Café", "Department store", "Furniture store", "Grocery or supermarket", "Meal delivery", "Meal takeaway", "Restaurant"};
            } else if (name == "Education") {
                typesList = new String[] {"School", "University"};
            } else if (name == "Entertainment") {
                typesList = new String[] {"Amusement park", "Aquarium", "Bowling alley", "Casino", "Movie rental", "Movie theater", "Moving company", "Night club"};
            } else if (name == "Health") {
                typesList = new String[] {"Dentist","Doctor", "Hospital", "Pharmacy", "Physiotherapist", "SPA"};
            } else if (name == "Library") {
                typesList = new String[] {"Library"};
            } else if (name == "Shops") {
                typesList = new String[] {"Clothing store", "Convenience store", "Electronics store", "Florist", "Hardware store", "Home goods store", "Jewelry store", "Liquor store", "Pet store", "Shoe store", "Shopping mall", "Store", "Grocery or supermarket"};
            } else if (name == "Money") {
                typesList = new String[] {"Accounting", "ATM", "Bank"};
            } else if (name == "Petrol Station") {
                typesList = new String[] {"Gas station"};
            } else if (name == "Police") {
                typesList = new String[] {"Police"};
            } else if (name == "Post Office") {
                typesList = new String[] {"Post office"};
            } else if (name == "Sport") {
                typesList = new String[] {"Gym", "Stadium"};
            } else if (name == "Underground Station") {
                typesList = new String[] {"Subway station"};
            } else if (name == "Tourist Attraction") {
                typesList = new String[] {"Art gallery", "Campground", "Museum", "Park", "RV park"};
            } else if (name == "Transport") {
                typesList = new String[] {"Airport", "Bus station", "Parking", "Taxi stand", "Train station", "Transit station"};
            }
        return typesList;
        }

    public void SavePreferences(Context context, String[] typesList, String name, int[] checked){
        SharedPreferences sharedPreferences = context.getSharedPreferences("RefineSettings", 2);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String checkedStr = "";
        for (int i = 0; i < checked.length; i++){
            checkedStr += checked[i] + " ";
        }
        editor.putString(name+"Checked", checkedStr);

        for (int i = 0; i < typesList.length; i++){
            if (typesList[i] == "Veterinary care"){
                editor.putString("vet", "veterinary_care");
            }
            if (typesList[i] == "Zoo"){
                editor.putString("zoo", "zoo");
            }
            if (typesList[i] == "Lodging"){
                editor.putString("lodging", "lodging");
            }
            if (typesList[i] == "Bakery"){
                editor.putString("bakery", "bakery");
            }
            if (typesList[i] == "Bar"){
                editor.putString("bar", "bar");
            }
            if (typesList[i] == "Café"){
                editor.putString("café", "café");
            }
            if (typesList[i] == "Department store"){
                editor.putString("department store", "department_store");
            }
            if (typesList[i] == "Furniture store"){
                editor.putString("furniture store", "furniture_store");
            }
            if (typesList[i] == "Grocery or supermarket"){
                editor.putString("grocery or supermarket", "grocery_or_supermarket");
            }
            if (typesList[i] == "Meal delivery"){
                editor.putString("meal delivery", "meal_delivery");
            }
            if (typesList[i] == "Meal takeaway"){
                editor.putString("meal takeaway", "meal_takeaway");
            }
            if (typesList[i] == "Restaurant"){
                editor.putString("restaurant", "restaurant");
            }
            if (typesList[i] == "School"){
                editor.putString("school", "school");
            }
            if (typesList[i] == "University"){
                editor.putString("university", "university");
            }
            if (typesList[i] == "Amusement park"){
                editor.putString("amusement park", "amusement_park");
            }
            if (typesList[i] == "Aquarium"){
                editor.putString("aquarium", "aquarium");
            }
            if (typesList[i] == "Bowling alley"){
                editor.putString("bowling alley", "bowling_alley");
            }
            if (typesList[i] == "Casino"){
                editor.putString("casino", "casino");
            }
            if (typesList[i] == "Movie rental"){
                editor.putString("movie rental", "movie_rental");
            }
            if (typesList[i] == "Movie theater"){
                editor.putString("movie theater", "movie_theater");
            }
            if (typesList[i] == "Moving company"){
                editor.putString("moving company", "moving_company");
            }
            if (typesList[i] == "Night club"){
                editor.putString("night club", "night_club");
            }
            if (typesList[i] == "Dentist"){
                editor.putString("dentist", "dentist");
            }
            if (typesList[i] == "Doctor"){
                editor.putString("doctor", "doctor");
            }
            if (typesList[i] == "Hospital"){
                editor.putString("hospital", "hospital");
            }
            if (typesList[i] == "Pharmacy"){
                editor.putString("pharmacy", "pharmacy");
            }
            if (typesList[i] == "Physiotherapist"){
                editor.putString("physiotherapist", "physiotherapist");
            }
            if (typesList[i] == "SPA"){
                editor.putString("spa", "spa");
            }
            if (typesList[i] == "Library"){
                editor.putString("library", "library");
            }
            if (typesList[i] == "Clothing store"){
                editor.putString("clothing store", "clothing_store");
            }
            if (typesList[i] == "Convenience store"){
                editor.putString("convenience store", "convenience_store");
            }
            if (typesList[i] == "Electronics store"){
                editor.putString("electronics store", "electronics_store");
            }
            if (typesList[i] == "Florist"){
                editor.putString("florist", "florist");
            }
            if (typesList[i] == "Hardware store"){
                editor.putString("hardware store", "hardware_store");
            }
            if (typesList[i] == "Home goods store"){
                editor.putString("home goods store", "home_goods_store");
            }
            if (typesList[i] == "Jewelry store"){
                editor.putString("jewelry store", "jewelry_store");
            }
            if (typesList[i] == "Liquor store"){
                editor.putString("liquor store", "liquor_store");
            }
            if (typesList[i] == "Pet store"){
                editor.putString("pet store", "pet_store");
            }
            if (typesList[i] == "Shoe store"){
                editor.putString("shoe store", "shoe_store");
            }
            if (typesList[i] == "Shopping mall"){
                editor.putString("shopping mall", "shopping_mall");
            }
            if (typesList[i] == "Store"){
                editor.putString("store", "store");
            }
            if (typesList[i] == "Grocery or supermarket"){
                editor.putString("grocery or supermarket", "grocery_or_supermarket");
            }
            if (typesList[i] == "Accounting"){
                editor.putString("accounting", "accounting");
            }
            if (typesList[i] == "ATM"){
                editor.putString("atm", "atm");
            }
            if (typesList[i] == "Bank"){
                editor.putString("bank", "bank");
            }
            if (typesList[i] == "Gas station"){
                editor.putString("gas station", "gas_station");
            }
            if (typesList[i] == "Police"){
                editor.putString("police", "police");
            }
            if (typesList[i] == "Post Office"){
                editor.putString("post office", "post_office");
            }
            if (typesList[i] == "Gym"){
                editor.putString("gym", "gym");
            }
            if (typesList[i] == "Stadium"){
                editor.putString("stadium", "stadium");
            }
            if (typesList[i] == "Subway station"){
                editor.putString("subway station", "subway_station");
            }
            if (typesList[i] == "Art gallery"){
                editor.putString("art gallery", "art_gallery");
            }
            if (typesList[i] == "Campground"){
                editor.putString("campground", "campground");
            }
            if (typesList[i] == "Museum"){
                editor.putString("museum", "museum");
            }
            if (typesList[i] == "Park"){
                editor.putString("park", "park");
            }
            if (typesList[i] == "RV park"){
                editor.putString("rv park", "rv_park");
            }
            if (typesList[i] == "Airport"){
                editor.putString("airport", "airport");
            }
            if (typesList[i] == "Bus station"){
                editor.putString("bus station", "bus_station");
            }
            if (typesList[i] == "Parking"){
                editor.putString("parking", "parking");
            }
            if (typesList[i] == "Taxi stand"){
                editor.putString("taxi stand", "taxi_stand");
            }
            if (typesList[i] == "Train station"){
                editor.putString("train station", "train_station");
            }
            if (typesList[i] == "Transit station"){
                editor.putString("transit station", "transit_station");
            }
        }
        editor.commit();



    }


    public boolean[] selectBoxes(ArrayList<Integer> n, int length){
        boolean[] checked = new boolean[length];
        for (int i = 0; i < n.size(); i++){
                checked[n.get(i)] = true;
        }
        return checked;
    }

    public ArrayList<Integer> savedSelected(String name, Context context, int length){
        SharedPreferences sharedPreferences = context.getSharedPreferences("RefineSettings", 2);
        ArrayList<Integer> savedNum = new ArrayList<>();
        String defaultStr = "";
        for (int i = 0; i < length; i++){
            defaultStr += i + " ";
        }

        String checkedStr = sharedPreferences.getString(name+"Checked", "");
        if (checkedStr == ""){
            savedNum = null;
        }
        else {
            String[] checkedStrArray = checkedStr.split(" ");

            for (int i = 0; i < checkedStrArray.length; i++){
                savedNum.add(Integer.parseInt(checkedStrArray[i]));
            }

        }
        if (savedNum != null){
            HashSet<Integer> hs=new HashSet<>(savedNum);
            savedNum = new ArrayList<>(hs);
            Collections.sort(savedNum);
        }

        return savedNum;
    }

    public ArrayList<Integer> defaultValues(int length, boolean isChecked){
        ArrayList<Integer> def = new ArrayList<>();
        if (isChecked) {
            for (int i = 0; i < length; i++) {
                def.add(i);
            }
        }
        return  def;
    }
}
