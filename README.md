# GradeFlow — CE316 Group Project

## Run

bash
mvn javafx:run

Database is created automatically at ~/GradeFlow/gradeflow.db.  
Extracted student files go to ~/GradeFlow/work/{projectId}/.

---

## Requirements Covered

| Req | Description                                           | Status      |
| --- | ----------------------------------------------------- | ----------- |
| R3  | Create / open project with configuration              | ✅          |
| R4  | Create, edit, delete, import, export configurations   | ✅          |
| R7  | Compile / interpret student source code               | ✅          |
| R8  | Compare output with expected output (EXACT / TRIMMED) | ✅          |
| R9  | Display per-student results with detailed logs        | ✅          |
| R5  | JSON import/export of configurations                  | ✅          |
| R6  | Batch ZIP processing (no manual one-by-one)           | ✅          |
| R10 | Open and save projects                                | ✅          |
| R1  | Windows installer                                     | Milestone 3 |
| R2  | Help manual (full HTML)                               | Milestone 3 |

---

## Known Limitation (prototype)

AssignmentProcessor currently runs only the _first_ test case per student.  
The TODO comment in AssignmentProcessor.java marks where the loop should go.
