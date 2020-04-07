/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp200a;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author opus
 */
public class Users extends ArrayList<User>{

    public Users(int initialCapacity) {
        super(initialCapacity);
    }

    public Users() {
    }

    public Users(Collection<? extends User> c) {
        super(c);
    }
    
    public User locateUser (String pwd){
        
        for (User u : this){
            if (u.getPassword().equals(pwd)) return u;
        }
        return null;
    }
    
}
