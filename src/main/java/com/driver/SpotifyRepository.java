package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User founduser(String mobile){
        User founduser = null;
        for(User user:users){
            if(user.getMobile()==mobile) {
                founduser = user;
                break;
            }
        }
        return founduser;
    }

    public Song foundsong(String title){
        Song foundsong = null;
        for(Song song:songs){
            if(song.getTitle()==title){
                foundsong = song;
                break;
            }
        }
        return foundsong;
    }

    public Playlist foundplaylist(String title){
        Playlist foundplaylist = null;
        for(Playlist playlist:playlists){
            if(playlist.getTitle()==title){
                foundplaylist = playlist;
                break;
            }
        }
        return foundplaylist;
    }

    public Album foundalbum(String title){
        Album foundAlbum = null;
        for(Album album:albums){
            if(album.getTitle()==title){
                foundAlbum = album;
                break;
            }
        }
        return foundAlbum;
    }

    public Artist foundartist(String name){
        Artist foundartist = null;
        for(Artist artist:artists){
            if(artist.getName()==name){
                foundartist = artist;
                break;
            }
        }
        return foundartist;
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;

    }

    public Album createAlbum(String title, String artistName) {
        //check if artist exists
        Artist foundArtist = foundartist(artistName);

        if(foundArtist==null)
            foundArtist = createArtist(artistName);

        Album album = new Album(title);
        List<Album> curr = artistAlbumMap.getOrDefault(foundArtist,new ArrayList<>());
        curr.add(album);
        artistAlbumMap.put(foundArtist,curr);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{

        Album foundalbum = foundalbum(albumName);
        if(foundalbum==null)
            throw new Exception("Album not found");

        Song song = new Song(title,length);
        songs.add(song);
        List<Song> curr = albumSongMap.getOrDefault(foundalbum,new ArrayList<>());
        curr.add(song);
        albumSongMap.put(foundalbum,curr);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Playlist playlist = new Playlist(title);
        List<Song> songlist = new ArrayList<>();
        for(Song song:songs){
            if(song.getLength()==length)
                songlist.add(song);
        }
        playlistSongMap.put(playlist,songlist);

        User founduser = founduser(mobile);

        if(founduser == null)
            throw new Exception("User does not exist");

        //add to listenermap
        List<User> curr = new ArrayList<>();
        curr.add(founduser);
        playlistListenerMap.put(playlist,curr);

        //add to creatormap
        creatorPlaylistMap.put(founduser,playlist);

        //user playlist map
        List<Playlist> cur = userPlaylistMap.getOrDefault(founduser,new ArrayList<>());
        cur.add(playlist);
        userPlaylistMap.put(founduser,cur);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Playlist playlist = new Playlist(title);
        List<Song> songlist = new ArrayList<>();
        for(Song song:songs){
            if(songTitles.contains(song))
                songlist.add(song);
        }
        playlistSongMap.put(playlist,songlist);

        User founduser = founduser(mobile);

        if(founduser == null)
            throw new Exception("User does not exist");

        //add to listenermap
        List<User> curr = new ArrayList<>();
        curr.add(founduser);
        playlistListenerMap.put(playlist,curr);

        //add to creatormap
        creatorPlaylistMap.put(founduser,playlist);

        //user playlist map
        List<Playlist> cur = userPlaylistMap.getOrDefault(founduser,new ArrayList<>());
        cur.add(playlist);
        userPlaylistMap.put(founduser,cur);

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {

        Playlist foundplaylist = foundplaylist(playlistTitle);
        if(!playlistListenerMap.containsKey(foundplaylist))
            throw new Exception("Playlist does not exist");

        User founduser = founduser(mobile);

        if(founduser == null)
            throw new Exception("User does not exist");

        //if user is creator of playlist
        if(creatorPlaylistMap.get(founduser)==foundplaylist)
            return foundplaylist;

        List<Playlist> curr = userPlaylistMap.getOrDefault(founduser,new ArrayList<>());

        //check if user is already a listener
        if(curr.contains(foundplaylist))
            return foundplaylist;

        curr.add(foundplaylist);
        userPlaylistMap.put(founduser,curr);

        List<User> cur = playlistListenerMap.getOrDefault(foundplaylist,new ArrayList<>());

        cur.add(founduser);
        playlistListenerMap.put(foundplaylist,cur);

        return foundplaylist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {

        User founduser = founduser(mobile);

        if(founduser == null)
            throw new Exception("User does not exist");

        Song foundsong = foundsong(songTitle);
        if(foundsong == null)
            throw new Exception("Song does not exist");

        List<User> curr = songLikeMap.getOrDefault(foundsong,new ArrayList<>());
        if(curr.contains(founduser))
            return foundsong;

        foundsong.setLikes(foundsong.getLikes()+1);

        curr.add(founduser);
        songLikeMap.put(foundsong,curr);

        Album foundalbum = null;
        for(Album album:albumSongMap.keySet()){
            if(albumSongMap.get(album).contains(foundsong)){
                foundalbum = album;
                break;
            }
        }

        if(foundalbum==null)return foundsong;

        Artist foundArtist = null;
        for(Artist artist:artistAlbumMap.keySet()){
            if(artistAlbumMap.get(artist).contains(foundalbum)){
                foundArtist = artist;
                break;
            }
        }

        if(foundArtist==null)return foundsong;

        foundArtist.setLikes(foundArtist.getLikes()+1);

        return foundsong;

    }

    public String mostPopularArtist() {
        String mostpop = "";
        int max = 0;
        for(Artist artist:artists){
            if(artist.getLikes()>max){
                max = artist.getLikes();
                mostpop = artist.getName();
            }
        }
        return mostpop;
    }

    public String mostPopularSong() {
        String mostpop = "";
        int max = 0;
        for(Song song:songs){
            if(song.getLikes()>max){
                max = song.getLikes();
                mostpop = song.getTitle();
            }
        }
        return mostpop;
    }
}
