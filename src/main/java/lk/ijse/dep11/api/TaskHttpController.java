package lk.ijse.dep11.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.to.TaskTo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;


@RestController
@RequestMapping("/tasks")
public class TaskHttpController {
    private final HikariDataSource pool;
    public  TaskHttpController(){
        HikariConfig config = new HikariConfig();
        config.setUsername("root");
        config.setPassword("mysql");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/to_do_app");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("maximumPoolSize",10);
         pool = new HikariDataSource(config);
    }

    @PreDestroy
    public void destroy(){
        pool.close();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json",consumes = "application/json")
    public TaskTo createTask(@RequestBody TaskTo taskTo){
        try (Connection connection = pool.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO task(description, status) VALUES(?,FALSE)", Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1,taskTo.getDescription());
            pstm.execute();
            ResultSet rst = pstm.getGeneratedKeys();
            rst.next();
            int id = rst.getInt(1);
            taskTo.setId(id);
            taskTo.setStatus(false);
            return taskTo;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping(produces = "application/json")
    public List<TaskTo> getAllTasks(){
        try (Connection connection = pool.getConnection()) {

            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM task ORDER BY id");
            LinkedList<TaskTo> taskList = new LinkedList<>();
            while(rst.next()){
                int id = rst.getInt("id");
                String description = rst.getString("description");
                boolean status = rst.getBoolean("status");
                taskList.add(new TaskTo(id,description,status));
            }
            return taskList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(value = "/{id}",consumes = "application/json")
    public void updateTask(@PathVariable int id,@RequestBody TaskTo task){

        try (Connection connection = pool.getConnection()) {
            PreparedStatement existTask = connection.prepareStatement("SELECT * FROM task WHERE id=?");
            existTask.setInt(1,id);
            if(!existTask.executeQuery().next()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found");
            }
            PreparedStatement pstm = connection.prepareStatement("UPDATE task SET description=?,status=? WHERE id=?");
            pstm.setString(1,task.getDescription());
            pstm.setBoolean(2,task.getStatus());
            pstm.setInt(3,id);
            pstm.execute();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
