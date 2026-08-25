#ifndef _APPSRC_BASE64_H
#define _APPSRC_BASE64_H


#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>

static inline size_t b64_encode_len(size_t inlen) {
    // [n/3] * 4
    return (( inlen + 3 - 1 ) / 3 ) * 4;
}
static inline size_t b64_decode_len(size_t inlen) {
    return (inlen / 4) * 3 - 2;
}

// out_len: out buffer max size
    // [in_len+1]
int b64_encode(const void *in, size_t in_len, void *out, size_t out_len);
// `in` must be string, so no need in_len
int b64_decode(const void *in, void *out, size_t out_len);

#ifdef __cplusplus
}
#endif

#endif