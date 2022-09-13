package server_part;

import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import common.DeleteFileMessage;
import common.FileMessage;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class DatabaseWorker {

    private Connection connection;

    private final String url = "jdbc:postgresql://localhost:5432/storage";

    private final String username = "admin";
    private final String password = "1";

    private final String tempFolder = "netty-server/filesServer";
    private StringBuilder sb;

    public DatabaseWorker() {
        initConnection();
        sb = new StringBuilder();
    }

    public boolean saveFile(FileMessage fm) {
        try {
            if (findFile(fm).next()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream(fm.getFile())) {
            PreparedStatement saveFile = connection.prepareStatement("INSERT INTO storage.public.files (file_name, content,file_owner) VALUES (?, ?, ?);");
            saveFile.setString(1, fm.getName());
            saveFile.setBinaryStream(2, fis);
            saveFile.setInt(3, Integer.parseInt(fm.getFileOwner()));
            saveFile.executeUpdate();
            System.out.println("save file");
            return true;
        } catch (Exception e) {
            System.out.println("unable to save file");
            return false;
        }
    }

    @SneakyThrows
    public boolean download(ChannelHandlerContext ctx, String s) {
        for (String fileName : Objects.requireNonNull(new File(tempFolder).list())) {
            File temp = new File(tempFolder + "\\" + fileName);
            temp.delete();
        }
        ResultSet rs = findFile(s.split(" "));
        if (rs.next()) {
            FileMessage fm = new FileMessage();
            String fileName = rs.getString("file_name");
            File temp = new File(tempFolder + "\\" + fileName);
            try (InputStream is = rs.getBinaryStream("content");
                 FileOutputStream fos = new FileOutputStream(temp)) {
                int b;
                while((b = is.read()) != -1) {
                    fos.write(b);
                }
                fm.setFile(temp);
                fm.setName(fileName);
                fm.setSize(temp.length());
                fm.setFileOwner(rs.getString("file_owner"));
                temp.delete();
                ctx.writeAndFlush(fm);
                return true;
            }
        }
        return false;
    }

    public boolean deleteFile(DeleteFileMessage dfm) throws SQLException {
        PreparedStatement deleteFile = connection.prepareStatement("delete from storage.public.files where file_name like ?;");
        deleteFile.setString(1, dfm.getName());
        return deleteFile.executeUpdate() > 0;
    }

    private ResultSet findFile(FileMessage fm) throws SQLException {
        PreparedStatement findFile = connection.prepareStatement("select * from storage.public.files where file_name like ? AND file_owner = ?;");
        findFile.setString(1, fm.getName());
        findFile.setInt(2, Integer.parseInt(fm.getFileOwner()));
        return findFile.executeQuery();
    }

    private ResultSet findFile(String[] prop) throws SQLException {
        sb.setLength(0);
        PreparedStatement findFile = connection.prepareStatement("select * from storage.public.files where file_name like ? AND file_owner = ?;");
        for (int i = 1; i < prop.length-1; i++) {
            sb.append(prop[i]).append(" ");
        }
        String fileName = sb.toString().trim();
        int fileOwner = Integer.parseInt(prop[prop.length-1]);
        findFile.setString(1, fileName);
        findFile.setInt(2, fileOwner);
        return findFile.executeQuery();
    }

    public boolean registration(ChannelHandlerContext ctx, String s) throws SQLException {
        String[] prop = s.split(" ");
        ResultSet rs = findUser(s);
        if (!rs.next()) {
            PreparedStatement registration = connection.prepareStatement("INSERT INTO storage.public.users(username, password) VALUES (?, ?);");
            registration.setString(1, prop[1]);
            registration.setString(2, prop[2]);
            registration.executeUpdate();
            ctx.writeAndFlush(new String[]{"/registration", prop[1], prop[2], String.valueOf(getUserId(prop))});
            return true;
        }
        return false;
    }

    public boolean login(ChannelHandlerContext ctx, String s) throws SQLException {
        ResultSet rs = findUser(s);
        String[] prop = s.split(" ");
        if (rs.next()) {
            if (rs.getString(2).equals(prop[1]) && rs.getString(3).equals(prop[2])) {
                ctx.writeAndFlush(new String[]{"/login", rs.getString(2), rs.getString(3), String.valueOf(rs.getInt(1))});
                return true;
            }
        }
        return false;
    }

    public void initConnection() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection succeed");
        } catch (SQLException e) {
            System.out.println("Unable to establish connection to DB");
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refresh(ChannelHandlerContext ctx, String s) throws SQLException {
        String[] prop = s.split(" ");
        PreparedStatement findFiles = connection.prepareStatement("select * from storage.public.files where file_owner = ?");
        findFiles.setInt(1, Integer.parseInt(prop[1]));
        ResultSet rs = findFiles.executeQuery();
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("/refresh");
        while (rs.next()) {
            fileNames.add(rs.getString(2));
        }

        String[] list= new String[fileNames.size()];
        ctx.writeAndFlush(fileNames.toArray(list));
    }

    private int getUserId(String[] prop) throws SQLException {
        PreparedStatement findId = connection.prepareStatement("select id from storage.public.users where username like ? AND password like ?");
        findId.setString(1,prop[1]);
        findId.setString(2,prop[2]);
        ResultSet rs = findId.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }

    private ResultSet findUser(String s) throws SQLException {
        String[] prop = s.split(" ");
        PreparedStatement findUser = connection.prepareStatement("select * from storage.public.users where username = ? AND password = ?;");
        findUser.setString(1, prop[1]);
        findUser.setString(2, prop[2]);
        return findUser.executeQuery();
    }

}
