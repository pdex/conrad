package pdex.conrad;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Booter {

  public static RepositorySystem newRepositorySystem() {
    return ManualRepositorySystemFactory.newRepositorySystem();
  }

  public static DefaultRepositorySystemSession newRepositorySystemSession( RepositorySystem system, PrintStream out ) {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

    //String path = "target/local-repo";
    String path = System.getProperty("user.home") + "/.m2/repository";

    LocalRepository localRepo = new LocalRepository( path );
    session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

    session.setTransferListener( new ConsoleTransferListener(out) );
    session.setRepositoryListener( new ConsoleRepositoryListener(out) );

    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    return session;
  }

  public static List<RemoteRepository> newRepositories( RepositorySystem system, RepositorySystemSession session ) {
    return new ArrayList<RemoteRepository>( Arrays.asList( newCentralRepository() ) );
  }

  private static RemoteRepository newCentralRepository() {
    return new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build();
  }
}
