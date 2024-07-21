import java.io.File

data class Task(var id:Int, var description:String, var status :Boolean = false)

class TaskManager(private val username : String,private val password : Int){
    private var tasks : MutableList<Task> = mutableListOf()
    private var nextId = 1
    private var fileName = "${username}-task.txt"

    init {
        loadTask()
    }

    private fun saveTask(){
        File(fileName).printWriter().use { out ->
            tasks.forEach { item ->
                out.println("${item.id}|${item.description}|${item.status}")
            }
        }
    }

    private fun loadTask(){
        val userTaskFile = File(fileName)
        if(userTaskFile.exists()){
            userTaskFile.readLines().forEach { line ->
                val parts = line.split("|")
                if(parts.size == 3){
                    val id = parts[0].toInt()
                    val description = parts[1]
                    val status = parts[2].toBoolean()
                    tasks.add(Task(id,description,status))
                    if(id >= 0) id + 1
                }
            }
        }
    }

    fun saveAccount(){
        val accountFile = "${username}-credential.txt"
        File(accountFile).appendText("${username}|${password}")
    }

    companion object{
        fun loadAccount(username: String,password: Int) : Boolean{
            val accountFile = File("${username}-credential.txt")
            if(accountFile.exists()){
                accountFile.readLines().forEach { line ->
                    val parts = line.split("|")
                    if (parts.size == 2 && parts[0] == username && parts[1].toInt() == password) return true
                }
            }
            return false
        }
    }

    fun addTask(description: String){
        val task = Task(nextId++,description)
        tasks.add(task)
        saveTask()
    }
    fun viewTask() : List<Task>{
        return  tasks.toList()
    }
    fun deleteTask(id: Int) : Boolean{
        val removed = tasks.removeIf{it.id == id}
        if (removed) {
            saveTask()
        }
        return removed
    }
    fun markTaskCompleted(id: Int) : Boolean{
        val task = tasks.find { it.id == id }
        return  if (task != null){
            task.status = true
            true
        }else{
            false
        }
    }
}

interface TaskAction{
    fun execute (taskManager: TaskManager)
}

class AddTask(private var description: String) : TaskAction{
    override fun execute(taskManager: TaskManager) {
        if(description.isNotBlank()){
            taskManager.addTask(description)
            println("Task added successfully")
        }else{
            println("Task must be filled")
        }
    }
}

class ViewTask : TaskAction{
    override fun execute(taskManager: TaskManager) {
        val tasks = taskManager.viewTask()
        if (tasks.isNotEmpty()){

            tasks.forEachIndexed{ index,item ->
                item.id = index + 1
                println("${item.id},${item.description},${item.status},")
            }
            println("Enter a task id to mark as completed if there is nothing completed insert 0: ")
            val id = readlnOrNull()?.toIntOrNull() ?: 0
            if(taskManager.markTaskCompleted(id)){
                println("Task marked as completed")
            }
        }else{
            println("You don't have any note yet")
        }
    }
}

class DeleteTask(private var id: Int) : TaskAction{
    override fun execute(taskManager: TaskManager) {
        if(taskManager.deleteTask(id)){
            println("Task deleted successfully")
        }else{
            println("Task ID not found in note")
        }
    }
}

class SaveAccount : TaskAction{
    override fun execute(taskManager: TaskManager) {
        taskManager.saveAccount()
    }
}

class RedirectToMainMenu : TaskAction{
    override fun execute(taskManager: TaskManager) {
        while (true){
            val menu : MutableList<String> = mutableListOf("[1].View Task\n","[2].Add Task\n","[3].Delete Task\n","[4].Exit\n","Which One Should I Execute ? : ")
            menu.forEach { item -> print(item) }

            val action : TaskAction? =  when(val choose = readlnOrNull()?.toIntOrNull() ?: 0){
                1 -> ViewTask()
                2 -> {
                    println("What do you want to keep note of? ")
                    val description = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "Blank"
                    if (description.isNotEmpty()){
                        AddTask(description)
                    }else{
                        null
                    }
                }
                3 -> {
                    taskManager.viewTask().forEach { item ->
                        println("${item.id}|${item.description}|${item.status}")
                    }
                    print("Which item to delete? (input task ID): ")
                    val id = readlnOrNull()?.toIntOrNull() ?: 0
                    if(taskManager.deleteTask(id)){
                        DeleteTask(id)
                    }else{
                        null
                    }
                }
                4 -> {
                    println("Have a nice day!")
                    break
                }
                else -> {
                    println("Sorry, there is no $choose in available menu")
                    null
                }
            }
            action?.execute(taskManager)
        }
    }
}

fun main() {
    while (true){
        print("[1] I am new here\n[2] I already have an account\nPlease input the option ID: ")
        when (val choose = readlnOrNull()?.toIntOrNull() ?: 0){
            1 -> {
                while (true){
                    try {
                        fun String.isNotLetter() : Boolean{
                            return !this.matches(Regex("^[a-zA-Z]+$"))
                        }
                        print("\n\nCreate Username: ")
                        val username = readlnOrNull()?.takeIf { it.isNotEmpty() } ?: "0"
                        if (username.isNotLetter()){
                            throw IllegalArgumentException("Username should be inserted, and make sure its not number")
                        }
                        print("Create password (numbers only): ")
                        val password = readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("This field needs password to continue")
                        print("Confirm password: ")
                        val confirmPassword = readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("This field needs password confirmation to continue")

                        if (password != confirmPassword){
                            throw IllegalArgumentException("Invalid password")
                        }else{
                            val taskManager = TaskManager(username,password)
                            if(TaskManager.loadAccount(username,password)){
                                println("Your account has been created before")
                                RedirectToMainMenu().execute(taskManager)
                            }
                            println("Successfully created an account, What can i do for you $username?")
                            SaveAccount().execute(taskManager)
                            RedirectToMainMenu().execute(taskManager)
                        }
                        break
                    }catch (error:IllegalArgumentException){
                        println(error.message)
                    }
                }
                break
            }
            2 -> {
                while (true){
                    try {
                        print("\n\nUsername : ")
                        val username = readlnOrNull()?.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Please insert the username")
                        print("Password : ")
                        val password = readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("Please insert the password")

                        if (TaskManager.loadAccount(username,password)){
                            println("Login success")
                            val taskManager = TaskManager(username,password)
                            RedirectToMainMenu().execute(taskManager)
                            break
                        }else{
                            throw  IllegalArgumentException("Invalid Username or Password")
                        }
                    }catch (error:IllegalArgumentException){
                        println(error.message)
                    }
                }
                break
            }
            else -> {
                println("There is no $choose in available options")
            }
        }
    }
}