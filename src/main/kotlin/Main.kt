import java.time.LocalDate

// Using ENUM for a specific set of constant values: Family Side -- Father or Mother
enum class FamilySide {FATHER, MOTHER}

// Using ENUM for a specific set of constant values: Relationship Type -- Blood or Other
enum class RelationType {BLOOD, OTHER}

// ********** DATA CLASS **********
data class Person(
    val name: String,                           // The person's name (immutable 'val' === cannot be reassigned)
    val side: FamilySide,                       // Family side (father or mother)
    val birthYear: Int,                         // Year of birth
    val deathYear: Int?,                        // Year of death (nullable int: person may still be alive
    val relationshipType: RelationType          // Relationship Type (blood or other)
)

// ********** FUNCTION lifespanYears **********
// This function calculates how long a person lived, or has lived so far.
// The return type is Int? --nullable Int-- , in case the data doesn't make sense -- like if the age is negative.
fun lifespanYears(p: Person, currentYear: Int = LocalDate.now().year): Int? {
    // If deathYear is null, use the current year (for still living)
    val end = p.deathYear ?: currentYear

    // Calculate the number of years lived.
    val years = end - p.birthYear

    // If result is negative (bad data), return null instead of a negative number.
    return if (years >= 0) years else null
}

// ********** FUNCTION averageLifespan **********
// This function calculated the average lifespan and returns null if the list is empty or has an invalid date.
fun averageLifespan(people: List<Person>): Double? {
    // mapNotNull applies the function to each person and removes any null results .
    val values = people.mapNotNull { lifespanYears(it) }

    // If the filtered list is empty, return null; otherwise return the average.
    return if (values.isEmpty()) {
        null
    } else {values.average()}
}

// ********** FUNCTION averageBySide **********
// This function Groups people by family side and calculates average lifespan per side.
fun averageBySide(people: List<Person>): Map<FamilySide, Double?> =
    FamilySide.entries.associateWith { side ->
        averageLifespan(people.filter {it.side == side})
    }

// ********** FUNCTION averageByRelation **********
// This function Groups people by relation type (blood/other) and calculates average lifespan.
fun averageByRelation(people: List<Person>): Map<RelationType, Double?> =
    RelationType.entries.associateWith { rel ->
        averageLifespan(people.filter {it.relationshipType == rel})
    }

// ********** FUNCTION formatAvg **********
// Formats average results neatly === like: Father's side: 79.9 years OR Other relatives: N/A
fun formatAvg(label: String, avg: Double?): String =
    if (avg == null) "$label: N/A" else "%s: %.1f years".format(label, avg)

// ********** FUNCTION averageFor (flexible filter **********
// This function returns the average lifespan for an optional side and/or relation filter
fun averageFor(
    people: List<Person>,
    side: FamilySide? = null,
    relation: RelationType? = null
): Double? {
    val filtered = people.filter {p ->
        (side == null || p.side == side) && (relation == null || p.relationshipType == relation)
    }
    return averageLifespan(filtered)
}

// ***********************************
// Functions for input/read
// ***********************************

// This function reads a non-empty string.
// Reads a single person's info card and prints their details.
fun readNonEmpty(prompt: String): String {
    while (true) {
            print(prompt)
            val s = readln().trim()
            if (s.isNotEmpty()) return s
                println("Please enter a value.")
    }
}

// This function read an Int within min--max (inclusive). Defaults to years from 1700...current.
// It wants a whole year, between 1700 and the current year, and it keeps asking until it gets one. prevents "12345" or "words"
fun readInt(prompt: String, min: Int = 1700, max: Int = LocalDate.now().year): Int {
    while (true) {
        print(prompt)
        val n = readln().trim().toIntOrNull()
        if (n != null && n in min..max) return n
        println("Enter a whole number between $min and $max.")
    }
}

// This function reads an optional Int within min--max. Empty input returns null. This gives the option to leave blank, for if still living.
fun readOptionalInt(prompt: String, min: Int, max: Int = LocalDate.now().year): Int? {
    while (true) {
        print("$prompt (Leave blank if living): ")
        val s = readln().trim()
        if (s.isEmpty()) return null
        val n = s.toIntOrNull()
        if (n != null && n in min..max) return n
        println("Enter a whole number in range, or leave blank.")
    }
}

// This function reads a familySide from user input -- F/M.
fun readSide(): FamilySide {
    while (true) {
        print("Side (F=Father, M=Mother): ")
        when (readln().trim().uppercase()) {
            "F", "FATHER" -> return FamilySide.FATHER
            "M", "MOTHER" -> return FamilySide.MOTHER
        }
        println("Please enter F or M.")
    }
}

// This function reads a relationType from user input -- B/O.
fun readRelation(): RelationType {
    while (true) {
        print("Relation (B=Blood, O=Other): ")
        when (readln().trim().uppercase()) {
            "B", "BLOOD" -> return RelationType.BLOOD
            "O", "OTHER" -> return RelationType.OTHER
        }
        println("Please enter B or O.")
    }
}

// ***********************************
// Functions for add/remove members
// ***********************************

// This function adds a family member.
fun addMemberInteractive(people: MutableList<Person>) {
    println("\nAdd number: ")
    val name = readNonEmpty("Name: ")
    val side = readSide()
    val birth = readInt("Birth year: ")
    val death = readOptionalInt("Death year", min = birth)
    val rel = readRelation()

    people += Person(name, side, birth, death, rel)
    println("✅ Added $name")
}

// This function removes a family member by exact case-sensitive name match.
fun removeMemberInteractive(people: MutableList<Person>) {
    if (people.isEmpty()) {
        println("No members to remove.")
        return
    }
    val target = readNonEmpty("\nRemove member - enter exact name: ")
    val idx = people.indexOfFirst { it.name.equals(target, ignoreCase = true) }
    if (idx == -1 ) {
        println("Not found: $target")
    } else {
        val removed = people.removeAt(idx)
        println("✅ Removed ${removed.name}")
    }
}

// ***********************************
// Functions for printSideRelationSummary
// ***********************************

// This function prints average lifespan for all Side x Relation combinations.
fun printSideRelationSummary(people: List<Person>) {
    println("\nSide x Relation Averages: ")
    val combos = listOf(
        "Father BLOOD" to averageFor(people, FamilySide.FATHER, RelationType.BLOOD),
        "Father OTHER" to averageFor(people, FamilySide.FATHER, RelationType.OTHER),
        "Mother BLOOD" to averageFor(people, FamilySide.MOTHER, RelationType.BLOOD),
        "Mother OTHER" to averageFor(people, FamilySide.MOTHER, RelationType.OTHER)
    )
    for ((label, avg) in combos) println(formatAvg(label, avg))
}

// ********** MAIN FUNCTION **********
fun main() {
    // Creating a sample list of people --- seed data
    val people = mutableListOf(
        Person("Bart", FamilySide.FATHER, 1927, 2013, RelationType.OTHER),
        Person("Donna", FamilySide.MOTHER, 1950, 2008, RelationType.BLOOD),
        Person("Glea", FamilySide.MOTHER, 1911, 2003, RelationType.BLOOD),
        Person("Norman 'Big Pat'", FamilySide.MOTHER, 1907, 1987, RelationType.OTHER),
        Person("Jim", FamilySide.MOTHER, 1932, 2024, RelationType.BLOOD),
        Person("Paulene", FamilySide.MOTHER, 1930, 2024, RelationType.BLOOD),
        Person("Jerry", FamilySide.MOTHER, 1937, 2023, RelationType.BLOOD),
        Person("Clyde", FamilySide.MOTHER, 1934, 1997, RelationType.BLOOD),
        Person("Anna", FamilySide.FATHER, 1909, 2000, RelationType.BLOOD),
        Person("Ulyss", FamilySide.FATHER, 1907, 1998, RelationType.BLOOD),
        Person("Pat", side = FamilySide.MOTHER, birthYear = 1979, deathYear = null, relationshipType = RelationType.BLOOD)
    )

    // Print a header
    println("~~~~~~~~~~ FAMILY LIFESPAN ANALYZER ~~~~~~~~~~")

    // Menu loop for user interaction
    while (true) {
        println(
            """
                👨‍👩‍👧‍👦⚪⚫🟤🟣🔵🟢 MENU 🟢🔵🟣🟤⚫⚪
                1) List Members
                2) Overall average
                3) Averages by side (Father/Mother)
                4) Averages by relation (Blood/Other)
                5) Side x Relation Combo (Father/Mother x Blood/Other)
                6) Add a member
                7) Remove a member
                8) Print EVERYTHING (all reports)
                0) EXIT
            """.trimIndent()
        )
        print("Select: ")
        when (readln().trim()) {
            // Option 1: Display all family members
            "1" -> {
                if (people.isEmpty()) {
                    println("No members yet.")
                } else {
                    println("\nFamily Members Entered: ")
                    println("--------------------------")
                    for (person in people) {
                        val deathDisplay = person.deathYear?.toString() ?: "Living"
                        val lifeDisplay = lifespanYears(person)?.toString() ?: "N/A"
                        println("${person.name} | Side: ${person.side} | Born: ${person.birthYear} | Died: $deathDisplay | Relation: ${person.relationshipType} | Lifespan: $lifeDisplay")
                    }
                    println("--------------------------")
                    println("Total family members listed: ${people.size}")
                    println("----------------------------------------------")
                    println("----------------------------------------------")
                }
            }
            // Option 2: Calculate and display overall average lifespan
            "2" -> {
                val overallAvg = averageLifespan(people)
                println("----------------------------------------------")
                println(formatAvg("Overall average lifespan (so far)", overallAvg))
                println("----------------------------------------------")
                println("----------------------------------------------")
            }
            // Option 3: Display average lifespan by familySide -- Father/Mother
            "3" -> {
                val bySide = averageBySide(people)
                println("----------------------------------------------")
                println(formatAvg("Father's side average", bySide[FamilySide.FATHER]))
                println(formatAvg("Mother's side average", bySide[FamilySide.MOTHER]))
                println("----------------------------------------------")
                println("----------------------------------------------")
            }
            // Option 4: Display average lifespans by relationType -- Blood/Other
            "4" -> {
                val byRelation = averageByRelation(people)
                println("----------------------------------------------")
                println(formatAvg("Blood relatives", byRelation[RelationType.BLOOD]))
                println(formatAvg("Other relatives", byRelation[RelationType.OTHER]))
                println("----------------------------------------------")
                println("----------------------------------------------")
            }
            // Option 5: Show a combined summary by both side and relation
            "5" -> {
                printSideRelationSummary(people)
            }
            // Option 6: Add a new member to the family list
            "6" -> addMemberInteractive(people)
            // Option 7: Remove a member by name
            "7" -> removeMemberInteractive(people)
            // Option 8: Print every report -- members, averages, summaries
            "8" -> {
                if (people.isEmpty()) {
                    println("No members yet")
                } else {
                    println("\nFamily Members Entered: ")
                    println("----------------------------------------------")
                    for (person in people) {
                        val deathDisplay = person.deathYear?.toString() ?: "Living"
                        val lifeDisplay = lifespanYears(person)?.toString() ?: "N/A"

                        println("----------------------------------------------")
                        println("${person.name} | Side: ${person.side} | Born: ${person.birthYear} | Died: $deathDisplay | Relation: ${person.relationshipType} | Lifespan: $lifeDisplay")
                        println("----------------------------------------------")
                        println("----------------------------------------------")
                    }
                    println("----------------------------------------------")
                    println("Total family members listed: ${people.size}")
                    println("----------------------------------------------")
                    println("----------------------------------------------")
                }
                // overall
                val overallAvg = averageLifespan(people)
                println("----------------------------------------------")
                println(formatAvg("Overall average lifespan (so far)",overallAvg))
                println("----------------------------------------------")
                println("----------------------------------------------")

                // by side
                val bySide = averageBySide(people)
                println("----------------------------------------------")
                println(formatAvg("Father's side average", bySide[FamilySide.FATHER]))
                println(formatAvg("Mother's side average", bySide[FamilySide.MOTHER]))
                println("----------------------------------------------")
                println("----------------------------------------------")

                // by relation
                val byRelation = averageByRelation(people)
                println("----------------------------------------------")
                println(formatAvg("Blood relatives", byRelation[RelationType.BLOOD]))
                println(formatAvg("Other relatives", byRelation[RelationType.OTHER]))
                println("----------------------------------------------")
                println("----------------------------------------------")

                // side x relation summary
                println("----------------------------------------------")
                printSideRelationSummary(people)
                println("----------------------------------------------")
                println("----------------------------------------------")
            }
            // Option 0: Exit the program
            "0" -> {
                println("Goodbye! Come back with more family names soon!")
                return
            }
            else -> println("Pick 0-8")
        }
    }
}