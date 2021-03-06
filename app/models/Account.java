package models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import play.Play;
import play.mvc.PathBindable;
import static play.data.validation.Constraints.*;
import javax.persistence.*;
import play.db.ebean.Model;
import models.UserFile.FileInformation;
import utils.CacheType.CacheType;
import utils.server.Configuration;


@Entity
public class Account extends Model {

    @Id
    private Long id;
    @Required
    private String userName;
    @OneToOne
    private LocalUser user;
    private String userDirPath;
    @OneToMany(mappedBy="account")
    private List<UserFile> userfiles;
    private Integer maxFilesSize;
    private Integer maxFilesCount;
    private Integer maxClonotypesCount;
    private Boolean privelegies;

    public Account(LocalUser user, String userName, String userDirPath) {
        this.user = user;
        this.userName = userName;
        this.userDirPath = userDirPath;
        this.userfiles = new ArrayList<>();
        this.maxClonotypesCount = Configuration.getMaxClonotypesCount();
        this.maxFilesSize = Configuration.getMaxFileSize();
        this.maxFilesCount = Configuration.getMaxFilesCount();
        this.privelegies = false;
    }

    public class AccountInformation {
        //Data accountInformation = new Data(new String[]{"email", "firstName", "lastName", "userName", "filesCount"});
        public String email;
        public String firstName;
        public String lastName;
        public String userName;
        public Integer filesCount;

        public AccountInformation(String email, String firstName, String lastName, String userName, Integer filesCount) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.userName = userName;
            this.filesCount = filesCount;
        }
    }

    public class FilesInformation {
        public List<UserFile.FileInformation> files;
        public Integer maxFileSize;
        public Integer maxFilesCount;
        public Boolean rarefactionCache;

        public FilesInformation(List<FileInformation> files, Integer maxFileSize, Integer maxFilesCount, Boolean rarefactionCache) {
            this.files = files;
            this.maxFileSize = getMaxFilesSize();
            this.maxFilesCount = getMaxFilesCount();
            this.rarefactionCache = rarefactionCache;
        }
    }

    public FilesInformation getFilesInformation() {
        List<UserFile.FileInformation> files = new ArrayList<>();
        for (UserFile userFile : getUserfiles()) {
            files.add(new UserFile.FileInformation(userFile.getFileName(), userFile.getSoftwareTypeName(), userFile.getRenderState().ordinal()));
        }

        File jsonFile = new File(getDirectoryPath() + "/" + CacheType.rarefaction.getCacheFileName() + ".cache");
        Boolean rarefactionCache = !jsonFile.exists();

        return new FilesInformation(files, maxFilesSize, maxFilesCount, rarefactionCache);
    }

    public Boolean isPrivilege() {
        return privelegies;
    }

    public Integer getMaxFilesSize() {
        return privelegies ? 0 : maxFilesSize;
    }

    public Integer getMaxFilesCount() {
        return privelegies ? 0: maxFilesCount;
    }

    public Integer getMaxClonotypesCount() {
        return privelegies ? 0: maxClonotypesCount;
    }

    public Boolean isMaxFilesCountExceeded() {
        if (getMaxFilesCount() > 0) {
            if (getFilesCount() > getMaxFilesCount()) {
                return true;
            }
        }
        return false;
    }

    public void cleanAllFiles() throws IOException {
        for (UserFile f : getUserfiles()) {
            UserFile.deleteFile(f);
        }
        File rarefactionCache = new File(getDirectoryPath() + "/" + CacheType.rarefaction.getCacheFileName() + ".cache");
        Files.deleteIfExists(rarefactionCache.toPath());
    }

    public List<String> getRenderedFileNames() {
        List<String> names = new ArrayList<>();
        for (UserFile userFile : getRenderedUserFiles()) {
            names.add(userFile.getFileName());
        }
        Collections.sort(names);
        return names;
    }

    public List<String> getFileNames() {
        List<String> names = new ArrayList<>();
        for (UserFile userFile : getUserfiles()) {
            names.add(userFile.getFileName());
        }
        Collections.sort(names);
        return names;
    }

    public List<Long> getRenderedFileSizes() {
        List<Long> sizes = new ArrayList<>();
        for (UserFile userFile : getRenderedUserFiles()) {
            File file = new File(userFile.getPath());
            sizes.add(file.length());
        }
        Collections.sort(sizes);
        return sizes;
    }

    public List<Long> getFileSizes() {
        List<Long> sizes = new ArrayList<>();
        for (UserFile userFile : getUserfiles()) {
            File file = new File(userFile.getPath());
            sizes.add(file.length());
        }
        Collections.sort(sizes);
        return sizes;
    }


    public AccountInformation getAccountInformation() {
        return new AccountInformation(this.user.email, this.user.firstName, this.user.lastName, this.userName, this.getFilesCount());
    }

    public List<UserFile> getUserfiles() {
        return userfiles;
    }

    public List<UserFile> getRenderedUserFiles() {
        List<UserFile> renderedUserFiles = new ArrayList<>();
        for (UserFile userfile : userfiles) {
            if (userfile.isRendered()) renderedUserFiles.add(userfile);
        }
        return renderedUserFiles;
    }

    public long getMaxSampleCount() {
        long max = 0l;
        for (UserFile userFile : getRenderedUserFiles()) {
            max = userFile.getSampleCount() > max ? userFile.getSampleCount() : max;
        }
        return max;
    }

    public String getUserName() {
        return userName;
    }

    public LocalUser getUser() {
        return user;
    }

    public String getDirectoryPath() {
        return userDirPath;
    }

    public Integer getFilesCount() {
        return getUserfiles().size();
    }

    public Long getId() {
        return id;
    }

    public String toString() {
        return String.format("%s", userName);
    }

    public static List<Account> findAll() {
        return find().all();
    }

    public static Account findByUserName(String userName) {
        return find().where().eq("userName", userName).findUnique();
    }

    public static Account findById(Long database_id) {
        return find().where().eq("id", database_id).findUnique();
    }

    public static Model.Finder<Long, Account> find() {
        return new Model.Finder<>(Long.class, Account.class);
    }
}
