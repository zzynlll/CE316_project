# GradeFlow - CE316 Group Project
GradeFlow is an Integrated Assignment Environment. It is an autonomous GUI tool created for
lecturers reviewing student submissions that allows users to build, save, and reuse
language-specific environment configuration files. The software supports both compiled and
interpreted languages, such as C, Java, Javascript, and Python. 
---

## Requirements Status

| Req | Description                                           | Status |
| --- | ----------------------------------------------------- | ------ |
| R2  | Help manual accessible from Help menu                 | ✅     |
| R3  | Create / open project with configuration              | ✅     |
| R4  | Create, edit, delete configurations; edit test cases  | ✅     |
| R5  | JSON import / export of configurations                | ✅     |
| R6  | Batch ZIP processing (entire directory, no manual)    | ✅     |
| R7  | Compile / interpret student source code               | ✅     |
| R8  | Compare output (EXACT / TRIMMED), all test cases      | ✅     |
| R9  | Display per-student results with detailed logs        | ✅     |
| R10 | Open and save projects at any time                    | ✅     |
| R1  | Windows installer                                     | ✅     |


---

## Architecture

```
gradeflow/
├── model/          — Configuration, Project, TestCase, StudentReport, enums
├── db/             — DatabaseManager (SQLite singleton, all CRUD)
├── manager/        — ProjectManager, ConfigurationManager (business logic)
├── processor/      — AssignmentProcessor, CommandExecutor, OutputComparator, ZipHandler
└── ui/             — MainWindow, ConfigurationDialog, NewProjectDialog,
                      ResultDetailsDialog, HelpWindow, Styles
```

### Grading pipeline 

1. Scan submissions directory for `.zip` files.
2. Extract each ZIP to `~/GradeFlow/work/{projectId}/{studentId}/`.
3. Verify source file is present (`MISSING_FILE` if not).
4. Compile with configured compiler (`COMPILE_ERROR` / `TIMEOUT` on failure).
5. For **every** test case: run the program, compare stdout to the expected output file.
6. Final status: `PASS` if all test cases match, `FAIL` otherwise (aggregated diff log).

Timeouts: 30 s compile · 10 s per test case execution.
