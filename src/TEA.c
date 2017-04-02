#include <stdio.h>
#include <jni.h>
#include "TEA.h"

void encrypt (long *v, long *k){
/* TEA decryption routine provided
   by ECE 422 eclass page. 2 longs are 
   decrypted at a time using a 4 long key */
unsigned long y = v[0], z=v[1], sum = 0;
unsigned long delta = 0x9e3779b9, n=32;

	while (n-- > 0){
		sum += delta;
		y += (z<<4) + k[0] ^ z + sum ^ (z>>5) + k[1];
		z += (y<<4) + k[2] ^ y + sum ^ (y>>5) + k[3];
	}

	v[0] = y;
	v[1] = z;
}

void decrypt (long *v, long *k){
/* TEA decryption routine provided
   by ECE 422 eclass page. 2 longs are 
   encrypted at a time using a 4 long key */
unsigned long n=32, sum, y=v[0], z=v[1];
unsigned long delta=0x9e3779b9l;

	sum = delta<<5;
	while (n-- > 0){
		z -= (y<<4) + k[2] ^ y + sum ^ (y>>5) + k[3];
		y -= (z<<4) + k[0] ^ z + sum ^ (z>>5) + k[1];
		sum -= delta;
	}
	v[0] = y;
	v[1] = z;
}


JNIEXPORT void JNICALL Java_TEA_encrypt
  (JNIEnv *env, jclass o, jbyteArray b, jlongArray k) {
	jboolean b_is_copy, k_is_copy;
	jsize len = (*env)->GetArrayLength(env, b);
    jlong *key = (*env)->GetLongArrayElements(env, k, &k_is_copy);
	jbyte *data = (*env)->GetByteArrayElements(env, b, &b_is_copy);

	long *ptr = (long *)data;
	for( ;(jbyte *)ptr < data + len; ptr+=2) {
		encrypt(ptr, (long *) key);
	}
	(*env)->ReleaseLongArrayElements(env, k, key, k_is_copy);
    (*env)->ReleaseByteArrayElements(env, b, data, b_is_copy);
}

JNIEXPORT void JNICALL Java_TEA_decrypt
  (JNIEnv *env, jclass o, jbyteArray b, jlongArray k) {
	jboolean b_is_copy, k_is_copy;
	jsize len = (*env)->GetArrayLength(env, b);
	jlong *key = (*env)->GetLongArrayElements(env, k, &k_is_copy);
	jbyte *data = (*env)->GetByteArrayElements(env, b, &b_is_copy);

	long *ptr = (long *)data;
	for( ;(jbyte *)ptr < data + len; ptr+=2) {
		decrypt(ptr, (long *)key);
	}
	(*env)->ReleaseLongArrayElements(env, k, key, k_is_copy);
    (*env)->ReleaseByteArrayElements(env, b, data, b_is_copy);
}

