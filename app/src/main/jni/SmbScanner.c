#include <jni.h>
#include <stdio.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <asm/ioctls.h>
jstring Java_com_ppfuns_filemanager_module_SmbScanner_getUdpSv(JNIEnv* env, jobject obj,jstring jstip,jint port) {
	const unsigned char b_netbios[] = { 0x0, 0x00, 0x0, 0x10, 0x0, 0x1, 0x0, 0x0, 0x0,
				0x0, 0x0, 0x0, 0x20, 0x43, 0x4b, 0x41, 0x41, 0x41, 0x41, 0x41,
				0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41,
				0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41,
				0x41, 0x41, 0x41, 0x0, 0x0, 0x21, 0x0, 0x1 };
	int sockfd;
	struct sockaddr_in addr;
	int addrlen = sizeof(struct sockaddr_in);
	if ((sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
		return (*env)->NewStringUTF(env, "socket error!");
	}
	memset(&addr,'\0',sizeof(struct sockaddr_in));
	const char* ip = (*env)->GetStringUTFChars(env, jstip, NULL);
	if (ip == NULL) {
		return (*env)->NewStringUTF(env, "get ip error!");
	}
	addr.sin_family = AF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.s_addr = inet_addr(ip);
	(*env)->ReleaseStringUTFChars(env,jstip,ip);
	int len = sendto(sockfd,b_netbios,50,0,(struct sockaddr *) (&addr),addrlen);
	struct timeval timeout;
	timeout.tv_sec = 1;
	timeout.tv_usec = 0;
	setsockopt(sockfd,SOL_SOCKET,SO_RCVTIMEO,(char *)&timeout.tv_sec,sizeof(struct timeval));
	char recvbuf[512];
	int rlen =recvfrom(sockfd,recvbuf,512,0,(struct sockaddr *) (&addr),&addrlen);
	close(sockfd);
	if(rlen>0){
		char pc_name[20];
		char pc_work[20];
		char pc_user[20];
		char pc_mac[20];
		int n_start, n_end;
		int num=0;
		int i;
		n_start = 57;
		n_end = n_start + 18;
		for (i = n_start; i < n_end; i++) {
			if(recvbuf[i]>=' '){
				pc_name[num] = (char) recvbuf[i];
				num++;
			}
		}
		n_start = 75;
		n_end = n_start + 18;
		num = 0;
		for (i = n_start; i < n_end; i++) {
			if (recvbuf[i] > ' ') {
				pc_work[num] = (char) recvbuf[i];
				num++;
			}
		}
		pc_work[num-1]='\0';
		n_start = n_end;
		n_end = n_start + 18;
		num = 0;
		for (i = n_start; i < n_end; i++) {
			if (recvbuf[i] > ' ') {
				pc_name[num] = (char) recvbuf[i];
				num++;
			}
		}
		pc_name[num]='\0';
		n_start = n_end;
		n_end = n_start + 6;
		num = 0;
		memset(pc_mac,'\0',sizeof(pc_mac));
		for (i = n_start; i < n_end; i++) {
			char tem2[4];
			sprintf(tem2, "%02X.", recvbuf[i]);
			strcat(pc_mac, tem2);
		}

		char ret[256];
		memset(ret,'\0',sizeof(ret));
		strcat(ret,pc_work);
		strcat(ret,"\|");
		strcat(ret,pc_name);
		strcat(ret,"\|");
		strcat(ret,pc_mac);
		return (*env)->NewStringUTF(env, ret);
	}else{
		return (*env)->NewStringUTF(env, "recvfrom server!");
	}
}

jstring Java_com_ppfuns_filemanager_module_SmbScanner_getTcpSv(JNIEnv* env, jobject obj,jstring jstip,jint port) {
	int sockfd;
	struct sockaddr_in addr;
	if ((sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
		return (*env)->NewStringUTF(env, "socket error!");
	}
	unsigned long ul = 1;
	ioctl(sockfd, FIONBIO, &ul);
	memset(&addr,'\0',sizeof(struct sockaddr_in));
	addr.sin_family = AF_INET;
	addr.sin_port = htons(port);
	const char* ip = (*env)->GetStringUTFChars(env, jstip, NULL);
	if (ip == NULL) {
		return (*env)->NewStringUTF(env, "get ip error!");
	}
	addr.sin_addr.s_addr = inet_addr(ip);
	(*env)->ReleaseStringUTFChars(env,jstip,ip);
	unsigned int ret = 0;
	fd_set fd;
	FD_ZERO(&fd);
	FD_SET(sockfd, &fd);
	struct timeval timeout;
	timeout.tv_sec = 1;
	timeout.tv_usec = 0;
	if (connect(sockfd, (struct sockaddr *) (&addr), sizeof(struct sockaddr))==-1) {
		int error = -1;
		int len = sizeof(int);
		if (select(sockfd + 1, NULL, &fd, NULL, &timeout) > 0) {
			getsockopt(sockfd, SOL_SOCKET, SO_ERROR, &error,(socklen_t *) &len);
			if (error == 0){
				ret = 1;
			}
			else{
				ret = 0;
			}
		} else{
			ret = 0;
		}
	} else{
		ret = 1;
	}
	ul = 0;
	ioctl(sockfd, FIONBIO, &ul);
	if (!ret) {
		close(sockfd);
		return (*env)->NewStringUTF(env, "connect error!");;
	}
	setsockopt(sockfd,SOL_SOCKET,SO_RCVTIMEO,(char *)&timeout.tv_sec,sizeof(struct timeval));
	char recvbuf[256];
	int rlen = recv(sockfd, recvbuf, sizeof(recvbuf), 0);
	close(sockfd);
	if(rlen>0){
		return (*env)->NewStringUTF(env, recvbuf);
	}else{
		return (*env)->NewStringUTF(env, "discover server!");
	}
}


