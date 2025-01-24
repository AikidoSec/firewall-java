import Pet
import java.sql.*

object DatabaseHelper {
    private fun getDBConnection(driver: String): Connection? {
        // The url specifies the address of our database along with username and password credentials
        val url = "jdbc:$driver://localhost:3306/db"
        val user = "user" // replace with your MySQL username
        val password = "password" // replace with your MySQL password
        try {
            return DriverManager.getConnection(url, user, password)
        } catch (e: SQLException) {
            println("Exception occurred in getDBConnection(): $e")
            return null
        }
    }

    val allPets: ArrayList<Any>
        get() {
            val pets = ArrayList<Any>()
            val conn = getDBConnection("mysql") ?: return pets
            try {
                val stmt = conn.prepareStatement("SELECT * FROM pets")
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    val id = rs.getInt("pet_id")
                    val name = rs.getString("pet_name")
                    val owner = rs.getString("owner")
                    pets.add(Pet(id, name, owner))
                }
            } catch (e: SQLException) {
                println("Exception occurred in getAllPets(): $e")
            }
            return pets
        }

    fun getPetById(id: Int): Pet? {
        val pets = ArrayList<Any>()
        val conn = getDBConnection("mysql") ?: return null
        try {
            val stmt = conn.prepareStatement("SELECT * FROM pets WHERE pet_id=?")
            stmt.setInt(1, id)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                val pet_id = rs.getInt("pet_id")
                val name = rs.getString("pet_name")
                val owner = rs.getString("owner")
                return Pet(pet_id, name, owner)
            }
        } catch (e: SQLException) {
            println("Exception occurred in getPetById(...): $e")
        }
        return Pet(0, "Unknown", "Unknown")
    }

    fun createPetByName(pet_name: String, driver: String): Int {
        val sql = "INSERT INTO pets (pet_name, owner) VALUES (\"$pet_name\", \"Aikido Security\")"
        val conn = getDBConnection(driver) ?: return 0
        try {
            val insertStmt = conn.prepareStatement(sql)
            return insertStmt.executeUpdate()
        } catch (e: SQLException) {
            println("Exception occurred in createPetByName(...): $e")
        }
        return 0
    }
}