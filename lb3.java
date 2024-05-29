import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class Agent {
    String url;
    int capacity;

    Agent(String url, int capacity) {
        this.url = url;
        this.capacity = capacity;
    }
}

public class LoadBalancer {
    private static List<Agent> agents = new ArrayList<>();
    private static HttpClient httpClient = new HttpClient();
    private static String inputUrl;
    private static int totalNumber;

    public static void main(String[] args) throws Exception {
        // Initialize HttpClient
        httpClient.start();

        // Set up Jetty server
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        server.setHandler(handler);

        // Add servlet for receiving agent data
        handler.addServlet(new ServletHolder(new AgentServlet()), "/agent");
        handler.addServlet(new ServletHolder(new StartLoadBalancerServlet()), "/start");

        // Start server
        server.start();
        server.join();
    }

    // Servlet to receive agent data
    public static class AgentServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            Gson gson = new Gson();
            BufferedReader reader = request.getReader();
            Type agentListType = new TypeToken<List<Agent>>() {}.getType();
            agents = gson.fromJson(reader, agentListType);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Agents received");
        }
    }

    // Servlet to start load balancing
    public static class StartLoadBalancerServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // 파일에서 URL과 숫자 읽기
            String inputFilePath = "input/input.txt";
            try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
                String input = br.readLine();
                String[] inputParts = input.split(" ");
                inputUrl = inputParts[0];
                totalNumber = Integer.parseInt(inputParts[1]);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Error reading the input file: " + inputFilePath);
                e.printStackTrace();
                return;
            }

            // 스레드 풀 생성
            ExecutorService executor = Executors.newFixedThreadPool(agents.size());

            List<Future<String>> futures = new ArrayList<>();

            // 각 에이전트에 대해 병렬로 HTTP 요청 보내기
            for (Agent agent : agents) {
                Future<String> future = executor.submit(() -> {
                    Request httpRequest = httpClient.POST(agent.url);
                    httpRequest.param("url", inputUrl);
                    httpRequest.param("totalNumber", String.valueOf(totalNumber));
                    ContentResponse httpResponse = httpRequest.send();
                    return httpResponse.getContentAsString();
                });
                futures.add(future);
            }

            // 각 요청의 응답 출력 및 별도 URL로 전송
            StringBuilder responseStringBuilder = new StringBuilder();
            for (int i = 0; i < agents.size(); i++) {
                try {
                    String result = futures.get(i).get();
                    System.out.println("Agent " + agents.get(i).url + " response: " + result);
                    responseStringBuilder.append(result).append("\n"); // 한 줄로 기록
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            // 별도의 URL로 응답 전송
            String responseString = responseStringBuilder.toString();
            sendResponseToURL(responseString);

            // 스레드 풀 종료
            executor.shutdown();

            // 응답 작성
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Load balancing started");
        }
    }

    // 별도의 URL로 응답을 POST 방식으로 전송
    private static void sendResponseToURL(String responseString) {
        String destinationURL = "https://example.com/receive";
        try {
            ContentResponse response = httpClient.POST(destinationURL)
                    .content(new StringContentProvider(responseString), "application/json")
                    .send();
            System.out.println("Response sent to " + destinationURL + ": " + response.getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
