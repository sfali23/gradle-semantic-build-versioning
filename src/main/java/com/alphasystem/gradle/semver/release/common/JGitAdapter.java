package com.alphasystem.gradle.semver.release.common;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Provides an adapter for interacting with a Git repository using the JGit library. This class
 * supports various Git-related operations such as retrieving commits, branches, tags, and analyzing
 * repository states.
 */
public class JGitAdapter {
  private final Repository repository;
  private Git git;

  public JGitAdapter(File workingDir) {
    this.repository = JGitAdapter.initRepository(workingDir);
  }

  public Repository getRepository() {
    return repository;
  }

  public Git getGit() {
    if (git == null) {
      git = new Git(repository);
    }
    return git;
  }

  public ObjectId getHeadCommit() throws IOException {
    return repository.resolve(Constants.HEAD);
  }

  private String getShortHash(ObjectId id) throws IOException {
    return repository.newObjectReader().abbreviate(id).name();
  }

  public String getShortHash() throws IOException {
    return getShortHash(getHeadCommit());
  }

  public String getCurrentBranch() throws IOException {
    return repository.getBranch();
  }

  public RevWalk getRevWalk() {
    return new RevWalk(repository);
  }

  public List<String> getTagsForCurrentBranch() throws GitAPIException {
    Map<ObjectId, List<org.eclipse.jgit.lib.Ref>> tags =
        getGit().tagList().call().stream()
            .collect(
                Collectors.groupingBy(
                    tagRef -> {
                      try {
                        return getRevWalk().parseCommit(getNonNullObjectId(tagRef)).getId();
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    }));

    try {
      ObjectId ref = repository.resolve(repository.getBranch());
      if (ref != null) {
        return StreamSupport.stream(getGit().log().add(ref).call().spliterator(), false)
            .flatMap(rev -> tags.getOrDefault(rev.getId(), Collections.emptyList()).stream())
            .map(tagRef -> tagRef.getName().replace(Constants.R_TAGS, ""))
            .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // Ignore exception and return empty list
    }
    return Collections.emptyList();
  }

  public List<String> getCommits() {
    try {
      ObjectId branchRef = repository.resolve(repository.getBranch());
      return StreamSupport.stream(getGit().log().add(branchRef).call().spliterator(), false)
          .map(RevCommit::getFullMessage)
          .collect(Collectors.toList());
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public List<String> getUnReleasedCommits(String start) {
    return getUnReleasedCommits(start, Constants.HEAD);
  }

  public List<String> getUnReleasedCommits(String start, String end) {
    try {
      ObjectId startId = repository.resolve(start);
      ObjectId endId = repository.resolve(end);
      RevWalk walk = getRevWalk();

      RevCommit startCommit = walk.parseCommit(startId);
      RevCommit endCommit = walk.parseCommit(endId);

      return StreamSupport.stream(
              getGit().log().addRange(startCommit, endCommit).call().spliterator(), false)
          .map(
              commit -> {
                String shortHash = null;
                try {
                  shortHash = getShortHash(commit.getId());
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
                String message = commit.getShortMessage();
                return "Commit(" + shortHash + ", " + message + ")";
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public List<String> getCommitBetween(String start) {
    return getCommitBetween(start, Constants.HEAD);
  }

  public List<String> getCommitBetween(String start, String end) {
    try {
      ObjectId startId = repository.resolve(start);
      ObjectId endId = repository.resolve(end);
      RevWalk walk = getRevWalk();

      RevCommit startCommit = walk.parseCommit(startId);
      RevCommit endCommit = walk.parseCommit(endId);

      return StreamSupport.stream(
              getGit().log().addRange(startCommit, endCommit).call().spliterator(), false)
          .map(RevCommit::getFullMessage)
          .collect(Collectors.toList());
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public boolean hasUncommittedChanges() {
    try {
      return getGit().status().call().hasUncommittedChanges();
    } catch (Exception e) {
      return false;
    }
  }

  public static JGitAdapter apply(File workingDir) {
    return new JGitAdapter(workingDir);
  }

  public static Repository initRepository(File workingDir) {
    return initRepository(workingDir, false);
  }

  public static Repository initRepository(File workingDir, boolean initialize) {
    try {
      if (initialize) {
        Git.init().setDirectory(workingDir).call();
      }

      FileRepositoryBuilder builder =
          new FileRepositoryBuilder().setWorkTree(workingDir).findGitDir(workingDir);

      File gitDir = builder.getGitDir();
      if (gitDir == null || !gitDir.exists()) {
        throw new RuntimeException(
            "Unable to find Git repository in: " + workingDir.getAbsolutePath());
      }

      if (!gitDir.getParentFile().getAbsolutePath().equals(workingDir.getAbsolutePath())) {
        builder.setWorkTree(gitDir.getParentFile());
      }

      return builder.build();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize repository", e);
    }
  }

  private static ObjectId getNonNullObjectId(Ref ref) {
    return Optional.ofNullable(ref.getPeeledObjectId()).orElseGet(ref::getObjectId);
  }
}
