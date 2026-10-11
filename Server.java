import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Server {

    static void send(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    static String verdict(int score) {
        if (score >= 60) {
            return "HIGH";
        } else if (score >= 30) {
            return "MEDIUM";
        } else if (score > 0) {
            return "LOW";
        }
        return "SAFE";
    }

    public static void main(String[] args) throws Exception {
        Db.init();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/check", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();

            if (query == null || !query.startsWith("text=")) {
                send(exchange, 400, "{\"error\": \"Use /check?text=your message\"}");
                return;
            }

            String text = URLDecoder.decode(query.substring(5), "UTF-8");
            Detector.Result r = Detector.analyze(text);

            try {
                Db.save(text, r.score, verdict(r.score));
            } catch (Exception e) {
                System.out.println("Could not save: " + e.getMessage());
            }

            StringBuilder matched = new StringBuilder("[");
            for (int i = 0; i < r.matched.size(); i++) {
                if (i > 0) {
                    matched.append(", ");
                }
                matched.append("\"").append(r.matched.get(i)).append("\"");
            }
            matched.append("]");

            String json = "{\"score\": " + r.score
                    + ", \"verdict\": \"" + verdict(r.score) + "\""
                    + ", \"matched\": " + matched + "}";
            send(exchange, 200, json);
        });
        server.createContext("/", exchange -> {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                send(exchange, 404, "{\"error\": \"Not found\"}");
                return;
            }
            byte[] page = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("index.html"));
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, page.length);
            OutputStream os = exchange.getResponseBody();
            os.write(page);
            os.close();
        });
        server.start();
        System.out.println("Running at http://localhost:8080/check?text=hello");
    }
}