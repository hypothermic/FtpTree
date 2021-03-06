package nl.hypothermic.ftptree;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class ftpTree {
	
	/** FtpTree - Create JTree from Ftp File List. Requires Apache Commons Net.
	 * @class ftpTree.java
	 * @author hypothermic
	 * @version v1.0
	 * https://github.com/hypothermic
	 * https://hypothermic.nl
	 */

	private JFrame frame;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ftpTree window = new ftpTree(".", "address", 21, "username", "password");
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace(); System.exit(1);
				}
			}
		});
	}
	
	private void initConn() throws SocketException, IOException {
		rc.connect(server, port);
		rc.login(usr, passwd);
	}
	
	private String server;
	private int port;
	private String usr;
	private String passwd;
	private FTPClient rc = new FTPClient();
	private String curRd;
	
	public ftpTree(String rd, String server, int port, String usr, String passwd) {
		this.server = server;
		this.port = port;
		this.usr = usr;
		this.passwd = passwd;
		this.curRd = rd;
		try { initConn(); } catch (Exception x) { x.printStackTrace(); System.exit(1); }
		initialize();
	}

	private void initialize() {
		frame = new JFrame(usr + "@" + server);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		// ftp pane
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 550, 300);
		frame.getContentPane().add(scrollPane);
		
		JTree fstree = new JTree(addFTPNodes(null, curRd));
		scrollPane.setViewportView(fstree);
		fstree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
		    }
		});
	}
	
	private DefaultMutableTreeNode addFTPNodes(DefaultMutableTreeNode parent, String cpath) {
		/** FTP */
		DefaultMutableTreeNode cdir;
		if (cpath == curRd) {
			cdir = new DefaultMutableTreeNode("> " + usr + "@" + server + ": " + cpath);
		} else {
			cdir = new DefaultMutableTreeNode(cpath + " (dir)");
		}
		if (parent != null) { parent.add(cdir); }
		FTPFile[] fl = null;
		try {
			fl = rc.listFiles(cpath);
		} catch (Exception e) {
			e.printStackTrace(); System.exit(1);
		}
		List<FTPFile> x = new ArrayList<FTPFile>();
		for (int i = 0; i < fl.length; i++) {
			x.add(fl[i]);
		}
		List<String> xs = new ArrayList<String>();
		for (int i = 0; i < fl.length; i++) {
			xs.add(fl[i].getName());
		}
		Vector<String> rs = new Vector<String>();
		for (FTPFile f : x) {
			if (f.isDirectory() && cpath.equals(".")) {
				// first launch: scan main dir
				addFTPNodes(cdir, f.getName());
			} else if (f.isDirectory() && !cpath.equals(".")) {
				// scan sub dir
				addFTPNodes(cdir, (cpath + File.separator + f.getName()));
			} else if (f.isFile()) {
				// "normal" file
	    	    rs.addElement(f.getName());
	    	} else if (f.isSymbolicLink()) {
	    		// linux symlink
	    	    rs.addElement(f.getName() + " (sym)");
	    	} else {
	    		// unidentified file
	    	    rs.addElement(f.getName() + " (unknown)");
	    	}
		}
		if (rs.size() == 0) {
			cdir.add(new DefaultMutableTreeNode("< no files >"));
		} else {
			for (int fnum = 0; fnum < rs.size(); fnum++) {
	    		cdir.add(new DefaultMutableTreeNode(rs.elementAt(fnum)));
	    }}
		return cdir; // temp 2 avoid err
	}

}
