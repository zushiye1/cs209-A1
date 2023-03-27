import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17.
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {
    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(
                        info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]),
                        Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]),
                        Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                        Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]),
                        Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                        Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        LinkedHashMap<String, Integer> result = this.courses.stream()
                .collect(Collectors.groupingBy((Course::getInstitution),
                        Collectors.summingInt(Course::getParticipants)))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (c1, c2) -> c1, LinkedHashMap::new));
        return result;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> result = this.courses.stream().collect(Collectors.groupingBy((Course::getA2), Collectors.summingInt(Course::getParticipants)));
        LinkedHashMap<String, Integer> collect2 = result.entrySet().stream()
                .sorted(((item1, item2) -> {
                    int compare = item2.getValue().compareTo(item1.getValue());
                    if (compare == 0) {
                        return   item1.getKey().compareTo(item2.getKey());
                    }
                    return compare;
                })).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return collect2;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> result = courses.stream()
                .flatMap(course -> Arrays.stream(course.getInstructors().split(",\\s*"))
                        .map(instructor -> new AbstractMap.SimpleImmutableEntry<>(instructor, course)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue,
                                Collectors.partitioningBy(
                                        course -> course.getInstructors().split(",\\s*").length == 1
                                )
                        )
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Arrays.asList(
                                entry.getValue().get(true).stream().map(Course::getTitle).distinct().sorted().collect(Collectors.toList()),
                                entry.getValue().get(false).stream().map(Course::getTitle).distinct().sorted().collect(Collectors.toList())
                        )
                ));
        return result;
    }


    //4
    public List<String> getCourses(int topK, String by) {
        List<String> result = new ArrayList<>();
        ArrayList<Course> temp = new ArrayList<>(courses);
        if (by.equals("hours")) {
            Collections.sort(temp, (o1, o2) -> {
                if (o2.totalHours > o1.totalHours)
                    return 1;
                else if (o1.totalHours == o2.totalHours)
                    return o1.title.compareTo(o2.title);
                else
                    return -1;
            });
        } else if (by.equals("participants")) {
            Collections.sort(temp, (o1, o2) -> {
                if (o2.participants > o1.participants) {
                    return 1;
                } else if (o1.participants == o2.participants) {
                    return o1.title.compareTo(o2.title);
                } else {
                    return -1;
                }
            });
        }
        int j = 0;
        for (int i = 0; i < topK; i++) {
            Course course = temp.get(j);
            j++;
            String name = course.title;
            if(!result.contains(name)){
            result.add(name);}
            else{
                i--;
            }
        }
        return result;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> result = this.courses.stream().filter(course -> course.subject.toLowerCase().contains(courseSubject.toLowerCase()))
                .filter(course -> course.percentAudited>=percentAudited)
                .filter(course -> course.totalHours<=totalCourseHours)
                .sorted(Comparator.comparing(Course::getTitle))
                .map(Course::getTitle)
                .distinct()
                .collect(Collectors.toList());
        return result;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        // Calculate the average Median Age, average % Male, and average % Bachelor's Degree or Higher for each course
        List<String> courseAverages = courses.stream()
                .collect(Collectors.groupingBy(Course::getNumber, Collectors.toList()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    double averageMedianAge = e.getValue()
                            .stream()
                            .mapToDouble(Course::getMedianAge)
                            .average()
                            .orElse(0);
                    double averageMale = e.getValue()
                            .stream()
                            .mapToDouble(Course::getPercentMale)
                            .average()
                            .orElse(0);
                    double averageBachelorOrHigher = e.getValue()
                            .stream()
                            .mapToDouble(Course::getPercentDegree)
                            .average()
                            .orElse(0);
                    return Arrays.asList(averageMedianAge, averageMale, averageBachelorOrHigher);
                }))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    return Math.pow(age - e.getValue().get(0), 2)
                            + Math.pow(gender * 100 - e.getValue().get(1), 2)
                            + Math.pow(isBachelorOrHigher * 100 - e.getValue().get(2), 2);

                }))
                .entrySet()
                .stream()

                .map( entry -> new AbstractMap.SimpleEntry<>(courses.stream()
                        .filter(c -> c.getNumber().equals(entry.getKey()))
                        .filter(c -> c.getLaunchDate() != null)
                        .max(Comparator.comparing(Course::getLaunchDate))
                                .orElse(null)
                        .title
                        ,
                        entry.getValue()))
                .sorted(Comparator.<Map.Entry<String, Double>>comparingDouble(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
//                .map(Object::toString)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());


//        List<String> result = new ArrayList<>();
//        int j = 0;
//        for (int i = 0; i < 10; i++) {
//            String tep = courseAverages.get(j);
//            j++;
//            if(!result.contains(tep)){
//                result.add(tep);}
//            else{
//                i--;
//            }
//        }
        return courseAverages;

    }



class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public String getNumber() {
        return number;
    }

    public String getInstructors() {
        return instructors;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    public String getInstitution() {
        return institution;
    }

    public int getParticipants() {
        return participants;
    }

    public String getA2(){
        return institution.concat("-").concat(subject);
    }

    public String getTitle() {
        return title;
    }

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}}