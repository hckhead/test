import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Agent {
    String url;
    int capacity;

    Agent(String url, int capacity) {
        this.url = url;
        this.capacity = capacity;
    }
}

public class LoadBalancer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 콘솔에서 한 줄로 URL과 숫자 입력받기
        System.out.println("Enter the URL and number (e.g., http://example.com 500):");
        String input = scanner.nextLine();
        String[] inputParts = input.split(" ");
        String url = inputParts[0];
        int totalNumber = Integer.parseInt(inputParts[1]);

        // agent.txt 파일에서 에이전트 정보 읽기
        List<Agent> agents = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("/Users/aiden/Repo/tct/sp/agent.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                agents.add(new Agent(parts[0], Integer.parseInt(parts[1])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 숫자를 에이전트의 용량에 따라 분배하기
        int remainingNumber = totalNumber;
        int currentStart = 0;

        for (Agent agent : agents) {
            if (remainingNumber <= 0) break;
            int allocated = Math.min(remainingNumber, agent.capacity);
            int currentEnd = currentStart + allocated - 1;
            System.out.println(agent.url + " " + currentStart + "~" + currentEnd);

            remainingNumber -= allocated;
            currentStart = currentEnd + 1;
        }

        if (remainingNumber > 0) {
            System.out.println("Not enough capacity to allocate all numbers.");
        }
    }
}
