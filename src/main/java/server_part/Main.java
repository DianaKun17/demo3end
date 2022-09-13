package server_part;

import java.sql.SQLException;

public class Main {

    private static DatabaseWorker db;

    public static void main(String[] args) {

        db = new DatabaseWorker();
        new Server(
                (fileMessage -> {
                    if (db.saveFile(fileMessage)) {
                        System.out.println(fileMessage.getName() + " stored in database");
                    }}),

                (ctx, s) -> {
                    try {
                        if (s.contains("/registration")) {
                            db.registration(ctx, s);
                        } else if (s.contains("/refresh")) {
                            db.refresh(ctx, s);
                        } else if (s.contains("/login")) {
                            db.login(ctx, s);
                        } else if (s.contains("/download")) {
                            db.download(ctx, s);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                },

                (dfm) -> {
                    try {
                        db.deleteFile(dfm);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });

    }
}