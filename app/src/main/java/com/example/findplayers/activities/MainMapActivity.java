package com.example.findplayers.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.example.findplayers.R;

import com.example.findplayers.classes.GameEvent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainMapActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private GoogleMap refMap;
    LatLng currentLatLngOnMap;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    ArrayList<GameEvent> gamesList = new ArrayList<GameEvent>();
    HashMap<String, Marker> gameMarkers = new HashMap<String, Marker>();
    HashMap<String, Marker> selectionMarkers = new HashMap<String, Marker>();
    Marker markerGPS;
    Marker markerUserSelection;
    ProgressBar myProgressBar;

    //Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uid = FirebaseAuth.getInstance().getUid();

    //CreateActivityDialog
    private EditText maxPlayers;
    private EditText notices;
    private String maxPlayersString;
    private String latString;
    private String lngString;
    private String startTimeString = "";
    private String endTimeString = "";
    private String dateString = "";
    private String typeString;
    private String noticesString;
    private Spinner spinner;
    private MaterialButton startTimeBtn;
    private MaterialButton endTimeBtn;
    private MaterialButton dateBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize view
        setContentView(R.layout.main_map);

        fragmentManager = getSupportFragmentManager();
        //initialize map fragment
        supportMapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.google_map);
        
        //get ref of google map object for future use
        //intialize fused location
        client = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        //progress bar for sign in
        myProgressBar = findViewById(R.id.main_progressbar);


        //show instruction dialog
        android.app.AlertDialog alertDialog;
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(MainMapActivity.this);
        LayoutInflater inflater = MainMapActivity.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.instruction_dialog, null);
        alertDialog = builder.setView(view).show();
        Button buttonOk = view.findViewById(R.id.warning_dialog_close_btn);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        //set on click on create activity button
        Button buttonCreateEvent = findViewById(R.id.buttonCreateEvent);
        buttonCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEventDialog();
            }
        });
        //set on click on button refresh
        FloatingActionButton buttonRefresh = findViewById(R.id.refresh_fab);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //animation
                Animation animation = AnimationUtils.loadAnimation(MainMapActivity.this,R.anim.rotate);
                buttonRefresh.startAnimation(animation);
                recreateMarkers();
            }
        });
    }

    //showing menu on the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    //func tha called when we select on on of the options in the menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout_item:{
                //sign out from firebase
                firebaseAuth.signOut();
                //transfer to login screen
                Intent intent = new Intent(MainMapActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //func in order to get current location on map ans set a marker
    private void getCurrentLocation() {
        //check permission before
        //when permission grated
        if (ActivityCompat.checkSelfPermission(MainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //initialize task location
            Task<Location> task = client.getLastLocation();
            //add listener when task get a value(lastLocation)
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //when success
                    if (location != null) {
                        //Async map
                        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                //when map is loaded
                                //set ref to the googleMap object for future use
                                refMap = googleMap;
                                //initialize latlng
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                //create marker options
                                markerGPS = googleMap.addMarker(new MarkerOptions().position(latLng).snippet("Marker selection"));
                                selectionMarkers.put("GPS", markerGPS);
                                //zoom map
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                //add marker on map
                                //set current latlng to this latlng
                                currentLatLngOnMap = latLng;

                                //listener for click on map
                                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                    @Override
                                    public void onMapClick(LatLng latLng) {
                                        //when clicked on map
                                        //remove markerGPS
                                        markerGPS.remove();
                                        selectionMarkers.remove("GPS");
                                        if (markerUserSelection != null) {
                                            markerUserSelection.remove();
                                            selectionMarkers.remove("User");
                                        }
                                        markerUserSelection = googleMap.addMarker(new MarkerOptions().position(latLng).snippet("Marker selection"));
                                        selectionMarkers.put("User", markerUserSelection);
                                        //animating to zoom the marker
                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                                latLng, 15
                                        ));
                                        //set current latlng to this latlng
                                        currentLatLngOnMap = latLng;

                                    }
                                });

                                //listener for click on marker
                                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker) {
                                        if (!marker.getSnippet().equals("Marker selection")) {
                                            showBottomDialogByEvent(marker.getSnippet());
                                        }
                                        return false;
                                    }
                                });

                                //i put it here to make it synchronously
                                readGamesListFromDB();
                            }
                        });
                    }
                }
            });
        } else {
            //when permission denied --> request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    //func that get called when user set a result to the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission granted --> call method
                getCurrentLocation();
            }
        }
    }

    //respnsible to set current location marker after user clicked on map
    public void updateCurrentLocation(View view) {
        if (ActivityCompat.checkSelfPermission(MainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //initialize task location
            Task<Location> task = client.getLastLocation();
            //add listener when task get a value(lastLocation)
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //when success
                    if (location != null) {
                        //here we remove marker of user if exists
                        if (markerUserSelection != null) {
                            markerUserSelection.remove();
                            selectionMarkers.remove("User");
                        }
                        markerGPS.remove();
                        selectionMarkers.remove("GPS");
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        //create marker options
                        markerGPS = refMap.addMarker(new MarkerOptions().position(latLng).snippet("Marker selection"));
                        selectionMarkers.put("GPS", markerGPS);
                        //zoom map
                        refMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        //add marker on map
                        //set current latlng to this latlng
                        currentLatLngOnMap = latLng;
                    }
                }
            });
        } else {
            //when permission denied --> request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    //func that get string of type event and return int of icon id
    public int iconIdByTypeEvent(String typeEvent) {
        int iconId;
        switch (typeEvent) {
            case "Basketball":
                iconId = R.drawable.basketball;
                break;
            case "Soccer":
                iconId = R.drawable.soccer;
                break;
            case "Running":
                iconId = R.drawable.running;
                break;
            case "Gym Training":
                iconId = R.drawable.gym;
                break;
            case "Tennis":
                iconId = R.drawable.tennis;
                break;
            case "Cards":
                iconId = R.drawable.cards;
                break;
            default:
                iconId = R.drawable.ic_baseline_my_location_24;
        }
        return iconId;
    }

    //add game event to list and push to DB
    public void addGameEvent(String ownerId, String type, String date, String startTime, String endTime, String maxPlayers, String notices, String locationLat, String locationLng) {
        GameEvent gameEvent = new GameEvent(ownerId, type, date, startTime, endTime, maxPlayers, notices, locationLat, locationLng);
        DatabaseReference gamesRef = database.getReference("games");
        gamesList.add(gameEvent);
        gamesRef.setValue(gamesList);//this action also do recreate markers
    }


    //func that read gameslist and call recreate program when program initialized and every time there is a change is games in db
    private void readGamesListFromDB() {
        DatabaseReference gamesRef = database.getReference("games");
        gamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gamesList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    gamesList.add(ds.getValue(GameEvent.class));
                }
                recreateMarkers();//func that use gameslist after we read her from DB
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //func that get up-to-date list and create markers of games accordingly
    public void recreateMarkers() {
        int iconId;
        Marker marker;
        //take care to clear all game markers on map and clear hash map of game markers
        if (!gameMarkers.isEmpty()) {
            removeAllGameMarkers();
            gameMarkers.clear();
        }
        //take care to clear selection markers on map and clear hash map of selection markers
        if (selectionMarkers.get("GPS") != null) {
            markerGPS.remove();
            selectionMarkers.remove("GPS");
        }
        if (selectionMarkers.get("User") != null) {
            markerUserSelection.remove();
            selectionMarkers.remove("User");
        }
        //create up-to-date game markers
        for (GameEvent gameEvent : gamesList) {
            iconId = iconIdByTypeEvent(gameEvent.getType());
            LatLng latLng = new LatLng(Double.parseDouble(gameEvent.getLocationLat()), Double.parseDouble(gameEvent.getLocationLng()));
            marker = refMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(iconId)).snippet(gameEvent.getGameId()));//snippet is the key of the marker
            gameMarkers.put(gameEvent.getGameId(), marker);
        }

    }

    //this func remove all the game event markers only! not the location markers
    private void removeAllGameMarkers() {
        for (Map.Entry<String, Marker> gameMarker : gameMarkers.entrySet()) {
            gameMarker.getValue().remove();
        }
    }

    //func that get lat lng and return full address in one string
    private String addressByLatLng(double lat, double lng) {
        Geocoder geocoder;
        List<Address> addresses = null;
        String fullAddress = "";
        //Address
        String address = "";
        String city = "";
        String country = "";
        String knownName = "";
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            //we get from this func list of addresses by the lat lng and we cares only from the 1st element
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            //city = addresses.get(0).getLocality();
            // country = addresses.get(0).getCountryName();

        } catch (IOException e) {
            e.printStackTrace();
        }

        fullAddress = address;
        return fullAddress;
    }

    //func showing bottom sheet dialog after user clicked on any game event
    public void showBottomDialogByEvent(String gameId) {
        final BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_dialog, null);
        GameEvent gameEventClicked = findGameByGameId(gameId);//gamesList[7]

        //Views
        TextView addressTv = bottomSheetView.findViewById(R.id.address_tv);
        TextView typeTv = bottomSheetView.findViewById(R.id.type_tv);
        TextView dateTv = bottomSheetView.findViewById(R.id.date_tv);
        TextView numPlayersTv = bottomSheetView.findViewById(R.id.num_players_tv);
        TextView startTimeTv = bottomSheetView.findViewById(R.id.start_time_tv);
        TextView endTimeTv = bottomSheetView.findViewById(R.id.end_time_tv);
        TextView noticesTv = bottomSheetView.findViewById(R.id.notices_tv);
        Button joinEventBtn = bottomSheetView.findViewById(R.id.join_event_btn);
        Button leaveEventBtn = bottomSheetView.findViewById(R.id.leave_event_btn);
        Button deleteEventBtn = bottomSheetView.findViewById(R.id.delete_event_btn);
        ImageView iconImg = bottomSheetView.findViewById(R.id.icon_img);


        //set info on the dialog
        if (gameEventClicked != null) {
            //set icon
            int iconId;
            iconId = iconIdByTypeEvent(gameEventClicked.getType());
            iconImg.setImageResource(iconId);
            //set address on tv
            String address;
            address = addressByLatLng(Double.parseDouble(gameEventClicked.getLocationLat()), Double.parseDouble(gameEventClicked.getLocationLng()));
            addressTv.setText(address);
            //set type
            typeTv.setText("Type: " + gameEventClicked.getType());
            //set date
            dateTv.setText("Date: " + gameEventClicked.getDate());
            //set max players
            numPlayersTv.setText("Number Of Players: " + gameEventClicked.getPlayersList().size() + "/" + gameEventClicked.getMaxPlayers());
            //set start time
            startTimeTv.setText("Start At: " + gameEventClicked.getStartTime());
            //set end time
            endTimeTv.setText("End At: " + gameEventClicked.getEndTime());
            //set notices
            noticesTv.setText("Notices: " + gameEventClicked.getNotices());

        }
        //if the owner of the game is this user connected to the app
        if (gameEventClicked.getOwnerId().equals(uid)) {
            deleteEventBtn.setVisibility(View.VISIBLE);
            joinEventBtn.setVisibility(View.GONE);
        }
        //if the owner of the game is this user and hes the only player in event
        if (gameEventClicked.getOwnerId().equals(uid) && gameEventClicked.getPlayersList().size() == 1) {
            leaveEventBtn.setVisibility(View.GONE);
        }
        //if the lid is over or the user connected is in this event allready
        if (gameEventClicked.getMaxPlayers().equals(gameEventClicked.getPlayersList().size() + "") || gameEventClicked.getPlayersList().contains(uid)) {
            joinEventBtn.setVisibility(View.GONE);
        }
        //if the user connected is in this event allready
        if (gameEventClicked.getPlayersList().contains(uid)) {
            leaveEventBtn.setVisibility(View.VISIBLE);
        }


        //set listeners for join,leave,delete event
        joinEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> playersUids = gameEventClicked.getPlayersList();
                playersUids.add(uid);
                gameEventClicked.setPlayersList(playersUids);
                gamesList.set(gamesList.indexOf(gameEventClicked), gameEventClicked);

                DatabaseReference gamesRef = database.getReference("games");
                gamesRef.setValue(gamesList);
                dialog.dismiss();
            }
        });

        leaveEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameEventClicked.getOwnerId().equals(uid) && gameEventClicked.getPlayersList().size() == 1){
                    Toast.makeText(MainMapActivity.this, "You Are The Only Player And The Host -> Please Delete The Event! ", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }else{
                    ArrayList<String> playersUids = gameEventClicked.getPlayersList();
                    playersUids.remove(uid);
                    gameEventClicked.setPlayersList(playersUids);
                    gamesList.set(gamesList.indexOf(gameEventClicked), gameEventClicked);

                    DatabaseReference gamesRef = database.getReference("games");
                    gamesRef.setValue(gamesList);
                    dialog.dismiss();
                }

            }
        });

        deleteEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gamesList.remove(gameEventClicked);
                DatabaseReference gamesRef = database.getReference("games");
                gamesRef.setValue(gamesList);
                dialog.dismiss();
            }
        });


        dialog.setContentView(bottomSheetView);
        dialog.show();

    }

    //this func return game event object by his gameID
    private GameEvent findGameByGameId(String gameId) {
        for (GameEvent gamegameEvent : gamesList) {
            if (gamegameEvent.getGameId().equals(gameId)) {
                return gamegameEvent;
            }
        }
        return null;
    }


    //create event dialog after user clicked create event
    private void createEventDialog() {
        android.app.AlertDialog alertDialog;
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_event_dialog, null);
        alertDialog = builder.setView(view).show();
        Button buttonPositive = view.findViewById(R.id.buttonOk);
        Button buttonNegative = view.findViewById(R.id.buttonCancel);

        //listeners for positive/negative btn
        buttonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxPlayersString = maxPlayers.getText().toString();
                noticesString = notices.getText().toString();
                latString = currentLatLngOnMap.latitude + "";
                lngString = currentLatLngOnMap.longitude + "";

                if (!maxPlayersString.isEmpty() && !startTimeString.isEmpty() && !endTimeString.isEmpty() && !dateString.isEmpty()) {
                    addGameEvent(uid, typeString, dateString, startTimeString, endTimeString, maxPlayersString, noticesString, latString, lngString);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(MainMapActivity.this, "Must fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        //views
        setupSpinnerAdapter(view);
        notices = view.findViewById(R.id.notices);
        maxPlayers = view.findViewById(R.id.maxPlayers);
        startTimeBtn = view.findViewById(R.id.startTime_btn);
        endTimeBtn = view.findViewById(R.id.endTime_btn);
        dateBtn = view.findViewById(R.id.date_btn);

        //set listener for date that user pick and set value to a string
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance(); //today's date
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(MainMapActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateBtn.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        dateString = dayOfMonth + "/" + (month + 1) + "/" + year;

                    }
                }, year, month, day);

                dpd.show(); //shows dialog
            }
        });


        //set listeners for start/end time that user pick and sets values to a strings
        startTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog tpd = new TimePickerDialog(MainMapActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        if (i1 < 10) {
                            startTimeString = i + ":0" + i1;
                        } else {
                            startTimeString = i + ":" + i1;
                        }
                        startTimeBtn.setText(startTimeString);
                    }
                }, hour, minute, true); //24h display
                tpd.show();
            }
        });
        endTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog tpd = new TimePickerDialog(MainMapActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        if (i1 < 10) {
                            endTimeString = i + ":0" + i1;
                        } else {
                            endTimeString = i + ":" + i1;
                        }
                        endTimeBtn.setText(endTimeString);
                    }
                }, hour, minute, true); //24h display
                tpd.show();
            }
        });


    }

    //spinner of typeActivity in createActivityDialog
    private void setupSpinnerAdapter(View view) {
        spinner = view.findViewById(R.id.spinner);
        List<String> list = new ArrayList<>();
        list.add("Basketball");
        list.add("Soccer");
        list.add("Tennis");
        list.add("Running");
        list.add("Gym Training");
        list.add("Cards");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.event_type_options, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeString = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}


