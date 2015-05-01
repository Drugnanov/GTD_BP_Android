
package cz.slama.android.gtd.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import cz.slama.android.gtd.utils.Compare;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class PersonCreate {

    @Expose
    private String name;
    @Expose
    private String password;
    @Expose
    private String username;
    @Expose
    private String surname;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (Compare.isNullOrEmpty(name)){
            this.name = null;
        }
        else{
            this.name = name;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (Compare.isNullOrEmpty(username)){
            this.username = null;
        }
        else{
            this.username = username;
        }
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        if (Compare.isNullOrEmpty(surname)){
            this.surname = null;
        }
        else{
            this.surname = surname;
        }
    }

//    @Override
//    public String toString() {
//        return ToStringBuilder.reflectionToString(this);
//    }
//
//    @Override
//    public int hashCode() {
//        return HashCodeBuilder.reflectionHashCode(this);
//    }
//
//    @Override
//    public boolean equals(Object other) {
//        return EqualsBuilder.reflectionEquals(this, other);
//    }

}
