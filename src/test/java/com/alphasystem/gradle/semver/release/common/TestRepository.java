package com.alphasystem.gradle.semver.release.common;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Random;

public class TestRepository {

    private static final Logger logger = LoggerFactory.getLogger(TestRepository.class);

    private final Repository repository;
    private final Git git;
    private final File workingDirectory;
    private final Random random = new Random();

    public TestRepository(Repository repository) {
        this.repository = repository;
        this.git = new Git(repository);
        this.workingDirectory = Paths.get(repository.getDirectory().getParentFile().getAbsolutePath()).toFile();
    }

    public void close() {
        repository.close();
    }

    public TestRepository commitAndTag(String tag) {
        return commitAndTag(tag, false);
    }

    public TestRepository commitAndTag(String tag, boolean annotated) {
        return commit().tag(tag, annotated);
    }

    public TestRepository tag(String tag) {
        return tag(tag, false);
    }

    public TestRepository tag(String tag, boolean annotated) {
        var tagCommand = git.tag()
                .setAnnotated(annotated)
                .setName(tag);

        if (annotated) {
            tagCommand = tagCommand.setMessage("Releasing " + tag);
        }

        try {
            tagCommand.call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tag: " + tag, e);
        }
        return this;
    }

    public TestRepository commit() {
        return commit("blah");
    }

    public TestRepository commit(String message) {
        try {
            git.commit()
                    .setAuthor("Batman", "batman@waynemanor.com")
                    .setMessage(message)
                    .call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create commit: " + message, e);
        }
        return this;
    }

    public TestRepository makeChanges() {
        String fileName = "file-" + generateRandomString(5);
        File file = new File(workingDirectory, fileName);
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(generateRandomString(50));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + fileName, e);
        }

        try {
            git.add().addFilepattern(fileName).call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add file: " + fileName, e);
        }
        return this;
    }

    public TestRepository add(String filePattern) {
        try {
            git.add().addFilepattern(filePattern).call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add file pattern: " + filePattern, e);
        }
        return this;
    }

    public TestRepository checkoutTag(String branchName, String tagName) {
        try {
            var ref = git.checkout()
                    .setCreateBranch(true)
                    .setName(branchName)
                    .setStartPoint(tagName)
                    .call();

            logger.trace("Checkout tag: {}, current branch: {}",
                    ref.getName(), getBranchName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to checkout tag: " + tagName + " to branch: " + branchName, e);
        }
        return this;
    }

    public TestRepository checkoutBranch(String branch) {
        try {
            git.checkout().setName(branch).call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to checkout branch: " + branch, e);
        }
        return this;
    }

    public TestRepository checkout(String revString) {
        try {
            return checkoutBranch(repository.resolve(revString).getName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to resolve revision: " + revString, e);
        }
    }

    public TestRepository branch(String name) {
        try {
            git.branchCreate().setName(name).call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create branch: " + name, e);
        }
        return this;
    }

    public TestRepository createAndCheckout(String name) {
        return branch(name).checkoutBranch(name);
    }

    public String getBranchName() {
        try {
            return repository.getBranch();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get branch name", e);
        }
    }

    public String getFullBranchName() {
        try {
            return repository.getFullBranch();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get full branch name", e);
        }
    }

    public TestRepository merge(String target) {
        try {
            var result = git.merge().include(repository.findRef(target)).call();
            logger.trace("Result of merge from {} to {} was {}",
                    getBranchName(), target, result.getMergeStatus().name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge from: " + target, e);
        }
        return this;
    }

    public TestRepository setOrigin(TestRepository origin) {
        try {
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new URIish(origin.repository.getDirectory().toURI().toURL()))
                    .call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set origin", e);
        }
        return this;
    }

    public String getHeadTag() {
        try {
            return git.describe().setTarget(repository.resolve(Constants.HEAD)).call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get head tag", e);
        }
    }

    public RevTag isHeadTagAnnotated() {
        try {
            return (RevTag) new RevWalk(repository)
                    .parseAny(repository.resolve(getHeadTag()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to check if head tag is annotated", e);
        }
    }

    public Optional<String> getHeadTagMessage() {
        try {
            RevWalk revWalk = new RevWalk(repository);
            var tagObject = revWalk.parseAny(repository.resolve(getHeadTag()));
            if (tagObject instanceof RevTag) {
                return Optional.of(((RevTag) tagObject).getFullMessage());
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get head tag message", e);
        }
    }

    // Static factory methods
    public static TestRepository apply(Repository repository) {
        return new TestRepository(repository);
    }

    public static TestRepository apply(File workingDir) {
        if (!workingDir.exists()) {
            workingDir.mkdirs();
        }
        return new TestRepository(JGitAdapter.initRepository(workingDir, true));
    }

    // Helper method for random string generation
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
