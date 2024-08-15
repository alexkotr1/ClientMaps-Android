package com.alexk.clientmap;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
public class Client implements Serializable {

    public Client() {}

    String name;
    String phone;
    String latitude;
    String longitude;
    String comments;
    String id;
    String[] names;
    String place;
    Boolean has_image;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] New_names) {
        names = New_names;
    }

    public String getPlace() { return this.place; }
    public Boolean has_image() { return this.has_image; }
    public void setPlace(String place) { this.place = place; }
    public void setHas_image(Boolean has_image) { this.has_image = has_image; }
    public void saveClientLocally(Context context) {
        try {
            List<Client> clients = getAllClients(context);
            clients.add(this);

            FileOutputStream fos = context.openFileOutput("clients_data", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(clients);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Client> getAllClients(Context context) {
        List<Client> clients = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput("clients_data");
            ObjectInputStream ois = new ObjectInputStream(fis);
            clients = (List<Client>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public void deleteLocalClient(Context context) {
        try {
            List<Client> clients = getAllClients(context);
            Client clientToRemove = null;

            for (Client client : clients) {
                if (client.getName().equals(this.getName())) {
                    clientToRemove = client;
                    break;
                }
            }

            if (clientToRemove != null) {
                clients.remove(clientToRemove);
            }

            FileOutputStream fos = context.openFileOutput("clients_data", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(clients);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}