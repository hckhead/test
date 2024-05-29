import java.io.*;
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

public class LoadBalancer{
    public static void main(String[] args) {
        // 파일에서 URL과 숫자 읽기
        String inputFilePath = "/Users/aiden/Repo/tct/sp/src/input/input.txt";
        String url = "";
        int totalNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String input = br.readLine();
            String[] inputParts = input.split(" ");
            url = inputParts[0];
            totalNumber = Integer.parseInt(inputParts[1]);
        } catch (IOException e) {
            System.out.println("Error reading the input file: " + inputFilePath);
            e.printStackTrace();
        }

        // agent.txt 파일에서 에이전트 정보 읽기
        List<Agent> agents = new ArrayList<>();
        String agentFilePath = "/Users/aiden/Repo/tct/sp/agent.txt"; // 파일 경로를 명시적으로 지정
        try (BufferedReader br = new BufferedReader(new FileReader(agentFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                agents.add(new Agent(parts[0], Integer.parseInt(parts[1])));
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + agentFilePath);
            e.printStackTrace();
        }

        // 결과를 파일에 저장하기
        String outputFilePath = "/Users/aiden/Repo/tct/sp/src/output/result.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {
            // 숫자를 에이전트의 용량에 따라 분배하기
            int remainingNumber = totalNumber;
            int currentStart = 0;

            for (Agent agent : agents) {
                if (remainingNumber <= 0) break;
                int allocated = Math.min(remainingNumber, agent.capacity);
                int currentEnd = currentStart + allocated - 1;
                writer.println(agent.url + " " + currentStart + "~" + currentEnd);

                remainingNumber -= allocated;
                currentStart = currentEnd + 1;
            }

            if (remainingNumber > 0) {
                writer.println("Not enough capacity to allocate all numbers.");
            }
        } catch (IOException e) {
            System.out.println("Error writing the output file: " + outputFilePath);
            e.printStackTrace();
        }
    }
}
