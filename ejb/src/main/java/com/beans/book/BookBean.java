package com.beans.book;


import com.beans.item.ItemBean;
import com.connection.DataSourceConnection;

import javax.ejb.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.toIntExact;

/**
 * Created by Veleri on 01.07.2016.
 */
public class BookBean extends ItemBean implements EntityBean {
    //private AuthorRemote author;
    private int authorID;

    private int pages;
    private int price;
    private int amount;
    private EntityContext context;

    public BookBean() {
    }

    public int getAmount() {
        return amount;
    }

    public int getPrice() {
        return price;
    }

    public int getPages() {
        return pages;
    }

    /*public AuthorRemote getAuthor() {
        return author;
    }*/
    public int getAuthorID() {
        return authorID;
    }

    public int getParentId()  {
        return parentId;
    }

    public Integer ejbFindByPrimaryKey(Integer key) throws FinderException {
        System.out.println("BookRemote bean method ejbFindByPrimaryKey(Integer key) was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM ITEM WHERE ID_ITEM=? AND TYPE = 0");
            statement.setInt(1, key);
            result = statement.executeQuery();
            if (result.next()) {
                return key;
            } else {
                throw new EJBException("Can't load data by id  due to SQLException");
            }
        } catch (SQLException e) {
            throw new EJBException("Can't load data by id  due to SQLException", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
    }

    public void setEntityContext(EntityContext entityContext) throws EJBException {
        System.out.println("BookRemote bean context was set.");
        this.context = entityContext;
    }

    public void unsetEntityContext() throws EJBException {
        System.out.println("BookRemote bean context was unset.");
        this.context = null;
    }

    public void ejbRemove() throws RemoveException, EJBException {
        System.out.println("BookRemote bean method ejbRemove() was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;
        this.setIdItem((Integer) context.getPrimaryKey());
        try {
            statement = connection.prepareStatement("{call  DELETEBOOK(?)}");
            statement.setInt(1, this.getIdItem());
            statement.execute();
        } catch (SQLException e) {
            throw new EJBException("Can't delete data due to SQLException", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
    }

    public void ejbActivate() throws EJBException {
        System.out.println("BookRemote bean was activated.");
        this.setIdItem((Integer) context.getPrimaryKey());
    }

    public void ejbPassivate() throws EJBException {
        System.out.println("BookRemote bean was passivated.");
    }

    public void ejbLoad() throws EJBException {
        System.out.println("BookRemote bean method ejbLoad() was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;
        this.setIdItem((Integer) context.getPrimaryKey());
        try {
            statement = connection.prepareStatement("SELECT i.ID_ITEM,i.NAME,rub.ID_ITEM AS \"RUBRIC\",a.ID_AUTHOR AS\"AUTHOR\",\n" +
                    "  p.PAGES,p.PRICE,p.AMOUNT,i.DESCRIPTION FROM ITEM i,PROPERTIES p,AUTHOR a,\n" +
                    "ITEM rub WHERE i.TYPE =0 AND i.ID_PROPERTIES=p.ID_BOOK\n" +
                    "AND p.ID_AUTHOR=a.ID_AUTHOR AND i.PARENT_ID=rub.ID_ITEM AND rub.TYPE=1\n" +
                    "AND i.ID_ITEM = ?");
            statement.setInt(1, this.getIdItem());
            result = statement.executeQuery();
            if (result.next()) {
                this.setType(ItemType.Book);
                this.setIdItem(result.getInt("ID_ITEM"));
                this.setName(result.getString("NAME"));
                this.setDescription(result.getString("DESCRIPTION"));
                this.setParentId(result.getInt("RUBRIC"));
                this.authorID = result.getInt("AUTHOR");
                this.pages = result.getInt("PAGES");
                this.price = result.getInt("PRICE");
                this.amount = result.getInt("AMOUNT");
            }
            System.out.println("BookRemote bean method ejbLoad() idAuthor="+getAuthorID()+" pages="+getPages()
                    +" price="+getPrice()+" amount="+ getAmount());
            System.out.println("BookRemote bean method ejbLoad() parID "+getParentId()+" name "+getName()
                    +" desc "+getDescription()+" idItem "+getIdItem());
        } catch (SQLException e) {
            throw new EJBException("Can't load data due to SQLException", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
    }

    public void ejbStore() throws EJBException {
        System.out.println("BookRemote bean method ejbStore() was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            statement = connection.
                    prepareStatement("UPDATE ITEM SET PARENT_ID=?,NAME=?,DESCRIPTION=? WHERE ID_ITEM = ? AND TYPE = 0");
            System.out.println("BookRemote bean method ejbStore() parID "+getParentId()+" name "+getName()
                    +" desc "+getDescription()+" idItem "+getIdItem());
            statement.setInt(1, getParentId());
            statement.setString(2, getName());
            statement.setString(3, getDescription());
            statement.setInt(4, getIdItem());
            statement.executeUpdate();
            System.out.println("BookRemote bean method ejbStore()  before if");

            Integer idP = propertiesId(getIdItem());
            System.out.println("BookRemote bean method ejbStore()  before if idP="+idP);
            if( idP != null) {
                statement = connection.
                        prepareStatement("UPDATE PROPERTIES SET ID_AUTHOR=?,PAGES=?,PRICE=?,AMOUNT=? WHERE ID_BOOK=?");
                System.out.println("BookRemote bean method ejbStore() idAuthor="+getAuthorID()+" pages="+getPages()
                        +" price="+getPrice()+" amount="+ getAmount());
                statement.setInt(1, getAuthorID());
                statement.setInt(2, getPages());
                statement.setInt(3, getPrice());
                statement.setInt(4, getAmount());
                statement.setInt(5, idP);
                statement.executeUpdate();
                System.out.println("BookRemote bean method ejbStore() in if");
            }
            System.out.println("BookRemote bean method ejbStore() finish");
        } catch (SQLException e) {
            throw new EJBException("Can't store data due to exception", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }

    }


    public void ejbHomeUpdateById(Integer id, String name, int author, String description,
                                  Integer rubric, int pages, int price, int amount) {
        System.out.println("BookRemote bean method ejbHomeUpdateById() was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;

        try {
            statement = connection.
                    prepareStatement("UPDATE ITEM SET PARENT_ID=?,NAME=?,DESCRIPTION=? WHERE ID_ITEM = ? AND TYPE = 0");
            statement.setInt(1, rubric);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setInt(4, id);
            statement.executeUpdate();


            Integer idP = propertiesId(id);
            if( idP != null) {
                statement = connection.
                        prepareStatement("UPDATE PROPERTIES SET ID_AUTHOR=?,PAGES=?,PRICE=?,AMOUNT=? WHERE ID_BOOK=?");
                statement.setInt(1, author);
                statement.setInt(2, pages);
                statement.setInt(3, price);
                statement.setInt(4, amount);
                statement.setInt(5, idP);
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new EJBException("Can't store data due to exception", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
    }

    private Integer propertiesId(Integer id) {

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("SELECT ID_PROPERTIES FROM ITEM WHERE ID_ITEM=? AND TYPE=0");
            statement.setInt(1, id);
            result = statement.executeQuery();

            if (result.next()) {
                return result.getInt("ID_PROPERTIES");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new EJBException("Can't store data due to exception", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
    }


    public Integer ejbCreateBook(String name, String description, int rubricId, int authorId, int pages, int price, int amount) throws CreateException {
        System.out.println("BookRemote bean method ejbCreateBook(String name, String description, int rubricId, int authorId, int pages, int price, int amount) was called.");
        System.out.println("-1 name "+name+" ,des "+description+" ,rub "+rubricId+" ,aut "+authorId+" ,pag "+pages+" ,prc "+price
        +" ,amount "+amount);

        long k;
        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("{call ADDBOOK(?,?,?,?,?,?,?)}"/*, statement.RETURN_GENERATED_KEYS*/);

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setInt(3, rubricId);
            statement.setInt(4, authorId);
            statement.setInt(5, pages);
            statement.setInt(6, price);
            statement.setInt(7, amount);
            statement.execute();

            /*result = statement.getGeneratedKeys();
              k = result.getLong(1);
              this.setIdItem(toIntExact(k));
             */
            statement = null;
            result = null;

            statement = connection.prepareStatement("SELECT MAX(ID_ITEM) FROM  ITEM");
            result = statement.executeQuery();
            if (result.next()) {
                this.setIdItem(result.getInt("MAX(ID_ITEM)"));
                System.out.println("Auto Generated Primary Key from resultSet k=" + getIdItem());


                this.setName(name);
                this.setDescription(description);
                this.setType(ItemType.Book);
                this.setParentId(rubricId);
                this.authorID = authorId;
                this.pages = pages;
                this.price = price;
                this.amount = amount;

                System.out.println("IdItem for new book =" + getIdItem());
            }
        } catch (SQLException e) {
            throw new EJBException("Can't create new data due to SQLException", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
        return getIdItem();
    }


    public void ejbPostCreateBook(String name, String description, int rubricId, int authorId, int pages, int price, int amount) throws CreateException {
        System.out.println("BookRemote bean method ejbPostCreateBook was called.");
    }


    public Collection ejbFindByName(String name) throws FinderException {
        System.out.println("BookRemote bean method ejbFindByName(String name) was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;
        List<Integer> lBook = new ArrayList<>();

        name = name.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");
        try {
            statement = connection.prepareStatement("SELECT i.ID_ITEM, i.NAME, rub.ID_ITEM AS \"RUBRIC\", a.ID_AUTHOR AS\"AUTHOR\",\n" +
                    "           p.PAGES, p.PRICE, p.AMOUNT, i.DESCRIPTION\n" +
                    "FROM ITEM i, PROPERTIES p, AUTHOR a, ITEM rub\n" +
                    "WHERE i.TYPE = 0\n" +
                    "      AND i.ID_PROPERTIES = p.ID_BOOK\n" +
                    "      AND p.ID_AUTHOR = a.ID_AUTHOR\n" +
                    "      AND i.PARENT_ID = rub.ID_ITEM\n" +
                    "      AND rub.TYPE = 1\n" +
                    "      AND lower(i.name || i.DESCRIPTION || a.NAME || a.SURNAME) like lower(?) ESCAPE '!'");
            System.out.println("-0");
            statement.setString(1, "%" + name + "%");
            result = statement.executeQuery();
            System.out.println("-1");
            while (result.next()) {
                this.setIdItem(result.getInt("ID_ITEM"));
                System.out.println("-2");
                lBook.add(this.getIdItem());
            }
        } catch (SQLException e) {
            throw new EJBException("Can't load data by id  due to SQLException", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
        return lBook;
    }


    public Collection ejbFindAllBooksByRubric(Integer id) throws FinderException {
        System.out.println("BookRemote bean method ejbFindAllBooksByRubric was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;
        List<Integer> lBooks = new ArrayList<>();
        try {
            statement = connection.prepareStatement("SELECT i.ID_ITEM,i.NAME,rub.ID_ITEM AS \"RUBRIC\",a.ID_AUTHOR AS\"AUTHOR\",\n" +
                    "p.PAGES,p.PRICE,p.AMOUNT,i.DESCRIPTION FROM ITEM i,PROPERTIES p,AUTHOR a," +
                    "ITEM rub WHERE i.TYPE =0 AND i.ID_PROPERTIES=p.ID_BOOK" +
                    " AND p.ID_AUTHOR=a.ID_AUTHOR AND i.PARENT_ID=rub.ID_ITEM AND rub.TYPE=1 AND rub.ID_ITEM=?");
            statement.setInt(1, id);
            result = statement.executeQuery();
            while (result.next()) {
                this.setIdItem(result.getInt("ID_ITEM"));
                lBooks.add(this.getIdItem());
            }
        } catch (SQLException e) {
            throw new EJBException("Can't get data for all items due to SQLException", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
        return lBooks;
    }


    public Collection ejbHomeGetAmountOfBooks(int amount) throws FinderException {
        System.out.println("BookRemote bean method ejbHomeGetAmountOfBooks(int amount) was called.");

        Connection connection = DataSourceConnection.getInstance().getConnection();
        ResultSet result = null;
        PreparedStatement statement = null;
        List<Integer> lBooks = new ArrayList<>();
        int page = 1;
        try {
            statement = connection.prepareStatement("select * from ( select a.*, ROWNUM rnum\n" +
                    "  from (SELECT i.ID_ITEM, i.NAME, rub.ID_ITEM AS \"RUBRIC\", a.ID_AUTHOR AS \"AUTHOR\", p.PAGES, p.PRICE, p.AMOUNT, i.DESCRIPTION\n" +
                    "        FROM ITEM i, PROPERTIES p, AUTHOR a, ITEM rub\n" +
                    "        WHERE i.TYPE = 0 AND i.ID_PROPERTIES = p.ID_BOOK AND p.ID_AUTHOR = a.ID_AUTHOR AND i.PARENT_ID = rub.ID_ITEM AND rub.TYPE = 1)a\n" +
                    "  where ROWNUM <=  ?)\n" +
                    "where rnum  >= ?");
            statement.setInt(1, amount);
            statement.setInt(2, page);
            result = statement.executeQuery();
            while (result.next()) {
                this.setIdItem(result.getInt("ID_ITEM"));
                lBooks.add(this.getIdItem());
            }
        } catch (SQLException e) {
            throw new EJBException("Can't get data for all items due to SQLException", e);
        } finally {
            DataSourceConnection.getInstance().disconnect(connection, result, statement);
        }
        return lBooks;
    }
}
