# Prompt Injection Detector

A small rule-based tool that checks text for common prompt injection phrases and returns a risk score. Built from scratch with Java, SQLite and a plain HTML/CSS/JavaScript page, with no frameworks.

Prompt injection is an attack where someone writes text such as "ignore previous instructions" to override the instructions given to an AI system. This project is a simple first line of defence that flags that kind of text.

## What it does

You type a message into the web page (or call the API directly). The detector lowercases the text and compares it against a list of known attack phrases, each with a weight. The weights of the matched phrases are added up (capped at 100) and turned into a verdict. Every check is saved to a SQLite database so there is a history of what was checked.

| Score     | Verdict |
|-----------|---------|
| 0         | SAFE    |
| 1 to 29   | LOW     |
| 30 to 59  | MEDIUM  |
| 60 to 100 | HIGH    |

## Tech

- **Java**: the detector logic and a REST API using the JDK's built-in `HttpServer`
- **SQLite via JDBC**: logs every check (text, score, verdict, timestamp)
- **HTML, CSS and JavaScript**: a simple page that calls the API with `fetch`

## Project structure

| File            | Purpose                                                              |
|-----------------|----------------------------------------------------------------------|
| `Detector.java` | Rules and scoring logic                                              |
| `Server.java`   | HTTP server, the `/check` endpoint, serves the web page              |
| `Db.java`       | Creates the table and saves checks; run it directly to print the log |
| `index.html`    | Front-end page                                                       |

## How to run

Requires JDK 11 or newer (developed on JDK 23) and the SQLite JDBC driver.

1. Download `sqlite-jdbc-3.36.0.3.jar` from Maven Central and put it in this folder.
2. Compile: `javac Server.java`
3. Start the server (Windows PowerShell): `java -cp ".;sqlite-jdbc-3.36.0.3.jar" Server`
   On Mac or Linux use `:` instead of `;` in the classpath.
4. Open http://localhost:8080/ in a browser.

To print the saved log: `java -cp ".;sqlite-jdbc-3.36.0.3.jar" Db`

## API

`GET /check?text=your message`

Example response:

```json
{"score": 40, "verdict": "MEDIUM", "matched": ["ignore previous instructions"]}
```

If the `text` parameter is missing, the API returns status 400 with an error message.

## Current rules

`ignore previous instructions`, `you are now`, `disregard`, `jailbreak`, `developer mode`, `pretend you`, `open database`, `system prompt`

## Limitations

- **False positives:** keyword matching cannot tell the difference between mentioning an attack phrase and using one. For example, "How do I write a system prompt for my project?" is flagged even though it is harmless.
- **Easy to evade:** rewording, extra spaces or character swaps such as "ign0re previous instructions" will slip past exact phrase matching.
- **English only**, and only the phrases in the rule list are detected.
- **Not machine learning:** the score comes from fixed weights I chose by hand, not from a trained model.
- Checked text is stored as typed in the local database, so it should not be used with sensitive messages.

## Ideas for next steps

- Normalise text before matching (collapse extra spaces, map look-alike characters)
- Use regular expressions for more flexible patterns
- Add a `POST` endpoint so long messages are not limited by URL length
- Add a page or endpoint to view the saved log
- Add automated tests for the detector

## Background

This project follows on from my research reports on cybersecurity risks in AI systems, where prompt injection is one of the threats analysed.

## Author

Jayant Tomar, B.Tech Computer Science and Engineering, Amity School of Engineering and Technology, AUUP
