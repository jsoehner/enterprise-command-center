# Project Memory Manager Skill

This skill instructs the agent on how to create, maintain, and utilize a `MEMORY.md` file at the root of a project. The `MEMORY.md` file serves as a persistent, long-term memory for the project, ensuring context, decisions, and preferences are carried over across different sessions.

## 🎯 Objective
To capture, maintain, and effectively use project context, architectural decisions, and user preferences within a `MEMORY.md` file.

## 📋 Triggers
- **Initialization:** When starting work on a new project that does not have a `MEMORY.md` file.
- **Explicit Request:** When the user says "save this to memory", "remember this", or "update memory".
- **Implicit Updates:** After a major architectural decision, a significant bug fix, or when the user states a clear preference.
- **Context Loading:** At the beginning of a new session, the agent should automatically review `MEMORY.md` to regain context.

## 🗂️ Structure of MEMORY.md
When creating or updating the `MEMORY.md` file, use the following structure:

```markdown
# Project Memory

## 📖 Project Context
A brief description of what the project is, its main goals, and the core technologies being used.

## 🎯 Current Objectives
- [ ] Short-term goals and active tasks.
- [ ] Known bugs or features currently being worked on.

## 🧠 Key Decisions & Architecture
- **[Date] - [Topic]:** Why a specific tool, library, or architectural pattern was chosen.
- **[Date] - [Topic]:** Explanation of complex or non-obvious code structures.

## 👤 User Preferences
- Preferred coding styles (e.g., camelCase vs snake_case).
- Testing frameworks or documentation standards.
- specific workflows or habits the user prefers.

## 📝 Unresolved Issues / Gotchas
- Known limitations of the current implementation.
- Quirks about the environment or deployment process.
```

## 🛠️ Agent Instructions (How to use this skill)

1. **Read Before Acting:** Always check for the existence of `MEMORY.md` at the root of the project at the start of a session. If it exists, read it to establish context before asking the user basic questions.
2. **Propose Updates:** When a significant conversation happens (a bug is fixed, a new preference is stated, a design is agreed upon), propose adding a summary to `MEMORY.md`. 
3. **Be Concise:** Keep the `MEMORY.md` file organized and concise. Remove completed objectives and consolidate old decisions if the file becomes too long.
4. **Append and Edit:** Use your file editing tools to append to or modify specific sections of the `MEMORY.md` file. Do not rewrite the entire file unless necessary.
5. **No Hallucinations:** Only add information that has been explicitly discussed or confirmed during the session.

## 🚀 Example Usage
**User:** "We decided to switch from Maven to Gradle for this project. Please remember this."
**Agent:** "I will update the `MEMORY.md` file under the 'Key Decisions' section to note the switch from Maven to Gradle, so we don't forget this in future sessions." *(Agent proceeds to use a file editing tool to update MEMORY.md)*
