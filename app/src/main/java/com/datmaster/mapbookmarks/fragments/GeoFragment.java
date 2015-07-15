package com.datmaster.mapbookmarks.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.datmaster.mapbookmarks.R;

import com.datmaster.mapbookmarks.activities.MainActivity;
import com.datmaster.mapbookmarks.adapters.PlaceHolderAutompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import com.google.android.gms.location.places.Places;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quinny898.library.persistentsearch.SearchBox;


public class GeoFragment extends Fragment
        implements TextWatcher, GoogleApiClient.OnConnectionFailedListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String LOG_TAG = "autocompletepp";

    private static View view;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private AutoCompleteTextView autoCompleteTextView;
    private PlaceHolderAutompleteAdapter mAdapter;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(50.2714317,30.3637758), new LatLng(50.9052795,30.9913697));

    public GeoFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
//                    .enableAutoManage(getActivity(), 0 /* clientId */, this)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
        mGoogleApiClient.connect();

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_geo, container, false);
        } catch (InflateException e) {

        }

        autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_places);
        autoCompleteTextView.addTextChangedListener(this);

        mAdapter = new PlaceHolderAutompleteAdapter(getActivity(), android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        autoCompleteTextView.setAdapter(mAdapter);

        autoCompleteTextView.setOnItemClickListener(mAutocompleteClickListener);

        ImageButton ib = (ImageButton) view.findViewById(R.id.imageButton_clear);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompleteTextView.setText("");
            }
        });

        return view;
    }


//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        LatLng sydney = new LatLng(50.4354162,30.5354371);
//
//        googleMap.setMyLocationEnabled(true);
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
//
//        googleMap.addMarker(new MarkerOptions()
//                .title("Метро")
//                .snippet("Кловська")
//                .position(sydney));
//    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getActivity().getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
//                mMap.setOnMyLocationButtonClickListener(this);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        autocompleteBegin(s.toString());
    }

    public void autocompleteBegin(String query) {

        LatLngBounds mBounds = new LatLngBounds(new LatLng(50.1976443,30.2951112), new LatLng(51.2142771,31.2262025));
        PendingResult result =
                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query,
                        mBounds, null);
        result.setResultCallback(new ResultCallback() {
            @Override
            public void onResult(Result result) {
                int c = 0;
            }
        });
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceHolderAutompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

//            Log.i(TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

//            Toast.makeText(getActivity().getApplicationContext(), "Clicked: " + item.description,
//                    Toast.LENGTH_SHORT).show();
//            Log.i(LOG_TAG, "Called getPlaceById to get Place details for " + item.placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            showMarker(place);
            // Format details of the place for display and show it in a TextView.
//            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
//                    place.getId(), place.getAddress(), place.getPhoneNumber(),
//                    place.getWebsiteUri()));

            // Display the third party attributions if set.
//            final CharSequence thirdPartyAttribution = places.getAttributions();
//            if (thirdPartyAttribution == null) {
//                mPlaceDetailsAttribution.setVisibility(View.GONE);
//            } else {
//                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
//                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
//            }
//
//            Log.i(LOG_TAG, TAG, "Place details received: " + place.getName());

            places.release();
        }
    };

    private void showMarker(Place place) {
        hideKeyboard();
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 13));
        mMap.addMarker(new MarkerOptions()
                .title(place.getName().toString())
                .snippet(place.getPhoneNumber().toString())
                .position(place.getLatLng()));
//        googleMap.setMyLocationEnabled(true);
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
//
//        googleMap.addMarker(new MarkerOptions()
//                .title("Метро")
//                .snippet("Кловська")
//                .position(sydney));
    }

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(LOG_TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(LOG_TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(getActivity(),
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
    }
}
