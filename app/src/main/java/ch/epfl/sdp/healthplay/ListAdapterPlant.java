package ch.epfl.sdp.healthplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ch.epfl.sdp.healthplay.database.Plant;

public class ListAdapterPlant extends ArrayAdapter<Plant> {

    public ListAdapterPlant(Context context, ArrayList<Plant> plantList){
        super(context, R.layout.item_plant_collection, plantList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        Plant plant = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_plant_collection, parent, false);
        }

        ImageView plantImage = convertView.findViewById(R.id.plantListImage);
        TextView plantName = convertView.findViewById(R.id.plantListName);
        TextView plantDate = convertView.findViewById(R.id.plantListDate);

        plantName.setText(plant.name);
        plantDate.setText(plant.date);
        Glide.with(getContext()).load(plant.imagePath).into(plantImage);

        return convertView;
    }
}
