package com.altester.core.serviceImpl.DataInit;

import java.util.Arrays;
import java.util.List;

public class DataConstants {

  public static final List<String> SURNAMES =
      Arrays.asList(
          "Novak",
          "Svoboda",
          "Dvorak",
          "Kucera",
          "Hajek",
          "Novotny",
          "Jelinek",
          "Vavra",
          "Dolezal",
          "Kratochvil",
          "Cerny",
          "Polak",
          "Kolar",
          "Zeman",
          "Kucera",
          "Fiala",
          "Havel",
          "Rehak",
          "Chlapek",
          "Jirasek",
          "Kral",
          "Urban",
          "Horak",
          "Novakova",
          "Tomek",
          "Kubik",
          "Houska",
          "Kott",
          "Horky",
          "Karas",
          "Bartoš",
          "Kalous",
          "Vacek",
          "Šimánek",
          "Pospisil",
          "Křen",
          "Dvořáková",
          "Michal",
          "Vesely",
          "Stepanek",
          "Dlouhy",
          "Prouza",
          "Sykora",
          "Čech",
          "Janda",
          "Kubát",
          "Kohout",
          "Pokorny",
          "Havelka",
          "Kunc",
          "Růžička",
          "Kříž",
          "Hrbek",
          "Zavadil",
          "Svoboda",
          "Toman",
          "Tůma",
          "Pojar",
          "Blaha",
          "Cerný",
          "Dušek",
          "Kozák",
          "Rajchert",
          "Pex",
          "Langer",
          "Beneš",
          "Chrást",
          "Mlynář",
          "Hrubý",
          "Krejčí",
          "Suchánek",
          "Jelínek",
          "Bauman",
          "Volný",
          "Hřebíček",
          "Dobrý",
          "Dědič",
          "Šťastný",
          "Hrubeš",
          "Patek",
          "Bažant",
          "Novotná",
          "Cejpek",
          "Sladký",
          "Mrázek",
          "Semerád",
          "Hanák",
          "Smrčka",
          "Knížek",
          "Pabian",
          "Vlk",
          "Černoch",
          "Schubert",
          "Řehák",
          "Fiala",
          "Doležalová",
          "Králová",
          "Straka",
          "Kolář",
          "Cikán");

  public static final List<String> FIRSTNAMES =
      Arrays.asList(
          "Jan",
          "Petr",
          "Lukas",
          "Tomas",
          "Martin",
          "Jakub",
          "Filip",
          "David",
          "Jiri",
          "Michal",
          "Roman",
          "Martin",
          "Vojtech",
          "Karel",
          "Pavel",
          "Radek",
          "Marek",
          "Jaroslav",
          "Ondrej",
          "Ladislav",
          "Josef",
          "Frantisek",
          "Adam",
          "Vladimir",
          "Stanislav",
          "Rostislav",
          "Jindrich",
          "Dominik",
          "Miroslav",
          "Robert",
          "Daniel",
          "Tomasz",
          "Patrik",
          "Milan",
          "Sven",
          "Alexander",
          "David",
          "Benjamin",
          "Petr",
          "Kamil",
          "Jozef",
          "Lukas",
          "Martin",
          "Igor",
          "Jakub",
          "Janek",
          "Dalibor",
          "Janusz",
          "Tibor",
          "Vít",
          "Marek",
          "Kamil",
          "Daniel",
          "Janek",
          "Vlastimil",
          "Michal",
          "Martin",
          "Stanislav",
          "Roman",
          "Lubomir",
          "Jiri",
          "Zdenek",
          "Petr",
          "Adrian",
          "Jindrich",
          "Adam",
          "Frantisek",
          "Alfred",
          "Libor",
          "Miloslav",
          "Dusan",
          "Roman",
          "Bohuslav",
          "Hugo",
          "Marian",
          "František",
          "Šimon",
          "Ladislav",
          "Norbert",
          "Václav",
          "Leoš",
          "Bohumil",
          "Karel",
          "Zbyněk",
          "Emanuel",
          "Dalibor",
          "Tibor",
          "Jirka",
          "Zdenek",
          "Kamil",
          "Jakub",
          "Emil",
          "František",
          "Eduard",
          "Jaromír",
          "Radoslav",
          "Dalibor",
          "Jiří",
          "Otakar",
          "René",
          "Igor",
          "Jan",
          "Petr",
          "Jarek",
          "Vojtěch");

  public static final List<String> GROUP_NAME_FORMATS =
      Arrays.asList(
          "CS-%d%s", // CS-1A
          "INF-%d%d%d", // INF-101
          "GROUP-%d%s", // GROUP-1A
          "%dIT%d", // 1IT1
          "SCI-%d%s", // SCI-1C
          "TECH-%d%d", // TECH-11
          "IS-%d%s", // IS-1A
          "%sPRG%d" // APRG1
          );

  public static final List<Character> GROUP_SUFFIXES = Arrays.asList('A', 'B', 'C', 'D', 'E');

  public static final List<String> SUBJECTS =
      Arrays.asList(
          "Introduction to Computer Science",
          "Programming Fundamentals",
          "Data Structures and Algorithms",
          "Database Systems",
          "Computer Networks",
          "Operating Systems",
          "Web Development",
          "Software Engineering",
          "Artificial Intelligence",
          "Computer Graphics",
          "Mobile Application Development",
          "Information Security",
          "Cloud Computing",
          "Machine Learning",
          "Human-Computer Interaction",
          "Computer Architecture",
          "Distributed Systems",
          "Big Data Analytics",
          "Formal Languages and Automata",
          "Object-Oriented Programming");

  public static final List<String> SHORT_NAMES =
      Arrays.asList(
          "ICS", "PF", "DSA", "DBS", "CN", "OS", "WD", "SE", "AI", "CG", "MAD", "IS", "CC", "ML",
          "HCI", "CA", "DS", "BDA", "FLA", "OOP");

  public static final List<String> TEST_TITLES_PROGRAMMING =
      Arrays.asList(
          "Basic Syntax and Control Structures",
          "Functions and Procedures",
          "Arrays and Collections",
          "Object-Oriented Programming Concepts",
          "Exception Handling",
          "File I/O Operations",
          "Recursion and Advanced Algorithms",
          "Memory Management",
          "Concurrency and Multi-threading",
          "Design Patterns Implementation");

  public static final List<String> TEST_TITLES_DATABASES =
      Arrays.asList(
          "SQL Basics and Data Retrieval",
          "Database Normalization",
          "Transactions and ACID Properties",
          "Indexing and Query Optimization",
          "ER Diagrams and Data Modeling",
          "Stored Procedures",
          "Triggers and Database Functions",
          "NoSQL Databases",
          "Database Security");

  public static final List<String> TEST_TITLES_NETWORKS =
      Arrays.asList(
          "OSI Model and TCP/IP",
          "Routing Protocols",
          "Network Security",
          "Wireless Networks",
          "IPv4 and IPv6 Addressing",
          "Subnetting",
          "Network Troubleshooting",
          "Client-Server Architecture",
          "VPN and Tunneling Protocols");

  public static final List<String> TEST_TITLES_OS =
      Arrays.asList(
          "Process Management",
          "Memory Management and Virtual Memory",
          "File Systems",
          "I/O Systems",
          "Deadlocks and Solutions",
          "Scheduling Algorithms",
          "Synchronization Mechanisms",
          "Security and Protection",
          "Distributed Operating Systems");

  public static final List<String> TEST_TITLES_AI =
      Arrays.asList(
          "Search Algorithms",
          "Knowledge Representation",
          "Machine Learning Basics",
          "Neural Networks",
          "Natural Language Processing",
          "Expert Systems",
          "Computer Vision",
          "Reinforcement Learning",
          "Genetic Algorithms and Evolutionary Computing");

  public static final List<String> TEST_TITLES_SE =
      Arrays.asList(
          "Software Development Lifecycle",
          "Requirements Engineering",
          "Software Design Principles",
          "Testing Methodologies",
          "Project Management",
          "Agile Development",
          "Version Control Systems",
          "Software Metrics",
          "Software Documentation");

  public static final List<String> CODING_QUESTIONS =
      Arrays.asList(
          "Write a function that finds the maximum value in an array",
          "Implement a binary search algorithm",
          "Create a class that represents a student with appropriate methods",
          "Write code to reverse a string without using built-in functions",
          "Implement a queue using two stacks",
          "Write a recursive function to calculate factorial",
          "Create a function that checks if a string is a palindrome",
          "Implement merge sort algorithm",
          "Write a function to check if a number is prime",
          "Create a linked list implementation with insert and delete methods");

  public static final List<String> DATABASE_QUESTIONS =
      Arrays.asList(
          "Write a SQL query to retrieve all employees who earn more than their department's average",
          "Design an ER diagram for a library management system",
          "Write a query to find the second highest salary in the employees table",
          "Explain the difference between INNER JOIN and LEFT JOIN with examples",
          "Write a stored procedure that updates employee salaries based on performance",
          "Create a query to list departments with more than 10 employees",
          "Design a normalized schema for an online store",
          "Write a trigger that logs all changes to the customer table",
          "Create an indexing strategy for a table with millions of records",
          "Write a query to pivot rows into columns");

  public static final List<String> NETWORKING_QUESTIONS =
      Arrays.asList(
          "Explain the TCP three-way handshake with a diagram",
          "Calculate the subnet mask for a network with 30 hosts",
          "Describe the difference between TCP and UDP protocols",
          "Explain how DNS resolution works",
          "Design a network for a small office with 50 workstations",
          "Troubleshoot a connectivity issue in a client-server environment",
          "Configure ACL rules for a firewall to protect a web server",
          "Explain how ARP protocol works with example",
          "Design a VLAN structure for a university campus",
          "Describe the process of routing between two networks");

  public static final List<List<String>> MULTIPLE_CHOICE_OPTIONS_JAVA =
      Arrays.asList(
          Arrays.asList(
              "public static void main(String[] args)",
              "public void main(String[] args)",
              "public static main(String[] args)",
              "public static void main()"),
          Arrays.asList("int x = 5;", "integer x = 5;", "Int x = 5;", "int x: 5;"),
          Arrays.asList("ArrayList", "LinkedList", "Vector", "Stack"),
          Arrays.asList(
              "try-catch-finally", "try-except-finally", "try-catch-end", "try-except-end"),
          Arrays.asList("extends", "implements", "inherits", "imports"),
          Arrays.asList("interface", "abstract class", "final class", "static class"),
          Arrays.asList("==", "equals()", "===", "isEqual()"),
          Arrays.asList("private", "protected", "public", "package"),
          Arrays.asList("final", "static", "const", "immutable"),
          Arrays.asList("StringBuilder", "StringBuffer", "String", "StringObject"));

  public static final List<List<String>> MULTIPLE_CHOICE_OPTIONS_DB =
      Arrays.asList(
          Arrays.asList("SELECT", "FIND", "EXTRACT", "QUERY"),
          Arrays.asList("PRIMARY KEY", "FOREIGN KEY", "UNIQUE KEY", "CANDIDATE KEY"),
          Arrays.asList("ACID", "BASE", "CHAIR", "TABLE"),
          Arrays.asList("INNER JOIN", "OUTER JOIN", "FULL JOIN", "CROSS JOIN"),
          Arrays.asList("DELETE", "DROP", "REMOVE", "TRUNCATE"),
          Arrays.asList("Index", "Catalog", "Dictionary", "Schema"),
          Arrays.asList("Transaction", "Process", "Thread", "Query"),
          Arrays.asList("DDL", "DML", "DCL", "DTL"),
          Arrays.asList("Normalization", "Optimization", "Indexing", "Partitioning"),
          Arrays.asList("MongoDB", "MySQL", "Oracle", "PostgreSQL"));

  public static final List<String> TEST_DESCRIPTIONS =
      Arrays.asList(
          "This test evaluates basic knowledge of the subject. Focus on fundamental concepts covered in lectures 1-5.",
          "Mid-term examination covering theoretical and practical aspects of chapters 1-7 in the textbook.",
          "Comprehensive assessment of both theoretical understanding and practical skills. All material from the course is included.",
          "Practical skills evaluation. Students should be prepared to solve real-world problems using techniques learned in the lab sessions.",
          "Quick knowledge check on recent topics. Covers only material from the last three weeks.",
          "Final examination for the semester. Review all lecture notes, assignments, and reading materials.",
          "Advanced concepts test aimed at distinguishing higher-performing students. Contains challenging problems.",
          "Foundational concepts review. This test helps ensure everyone has the necessary base knowledge for upcoming topics.",
          "Applied knowledge test focused on implementation rather than theory. Reference materials are permitted.",
          "Specialized topic assessment focusing on the most recent module. Previous modules are not included.");
}
