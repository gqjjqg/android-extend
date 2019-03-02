/*
 * cache.c
 *
 *  Created on: 2014年11月17日
 *      Author: gqj3375
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "cache.h"
#include "cache_data.h"
#include "rbtree.h"
#include "dllist.h"

#include "logger.h"

typedef struct CACHE_t {
	// RB-tree
	RB_NODE  mRBNode;

	// link queue
	DL_NODE mDLNode;

	// key-value
	long	mKey;
	DATA	mData;
}CACHE_NODE, *LPCACHE_NODE;

typedef struct cache_handle_t {
	int mMaxCount;
	int mCurCount;

	//link queue
	DL_ROOT mDLRoot;

	//RB-tree
	RB_ROOT mRBRoot;

}CACHE_HANDLE, *LPCACHE_HANDLE;

#define GMemMalloc		malloc
#define GMemFree		free
#define GNull			NULL

#if defined(offsetof)
#undef offsetof
#endif
#define offsetof(TYPE, MEMBER) ((size_t) &((TYPE *)0)->MEMBER)

#if defined(container_of)
#undef container_of
#endif
#define container_of(res, ptr, type, member) { \
		unsigned long address = (unsigned long)(ptr); \
		res = (type *)( address - offsetof(type,member) ); }

static LPCACHE_NODE rbt_search(LPRB_ROOT root, int hash);
static int rbt_insert(LPRB_ROOT root, LPCACHE_NODE data);

unsigned long CreateCache(int size)
{
	LPCACHE_HANDLE handle;
	handle = (LPCACHE_HANDLE)GMemMalloc(sizeof(CACHE_HANDLE));
	if (handle == GNull) {
		return 0;
	}

	handle->mCurCount = 0;
	handle->mMaxCount = size;
	handle->mDLRoot.dl_head = GNull;
	handle->mDLRoot.dl_last = GNull;
	handle->mRBRoot.rb_node = GNull;

	return (unsigned long)handle;
}

int PushCache(unsigned long h, int hash, int width, int height, int format, unsigned char * data)
{
	LPCACHE_NODE pNode;
	LPCACHE_HANDLE handle = (LPCACHE_HANDLE)h;
	int ret = 0;
	if (handle == GNull) {
		return -1;
	}

	// search in rb-tree
	pNode = rbt_search(&handle->mRBRoot, hash);

	if (handle->mCurCount >= handle->mMaxCount && pNode == GNull) {
		// replace
		container_of(pNode, dl_last(&(handle->mDLRoot)), CACHE_NODE, mDLNode);
#if defined( _DEBUG )
		LOGI("replace get last 0x%X\n", pNode);
#endif
	}

	if (pNode != GNull) {
		//remove out in linked queue.
		dl_remove_node(&(pNode->mDLNode), &(handle->mDLRoot));

		//remove from rb-tree.
		rb_erase(&pNode->mRBNode, &handle->mRBRoot);

		pNode->mKey = hash;
		
	} else {
		pNode = (LPCACHE_NODE)GMemMalloc(sizeof(CACHE_NODE));
		pNode->mKey = hash;
		handle->mCurCount++;
		cache_data_initial(&(pNode->mData));
	}

	cache_data_update(&(pNode->mData), width, height, format, data);

	//add node
	dl_insert_node(&(pNode->mDLNode), GNull, &(handle->mDLRoot));

	//add to rb-tree
	rb_init_node(&pNode->mRBNode);
	rbt_insert(&handle->mRBRoot, pNode);
	return ret;
}

int QueryCache(unsigned long h, int hash, int *width, int *height, int *format)
{
	LPCACHE_NODE pNode;
	LPCACHE_HANDLE handle = (LPCACHE_HANDLE)h;
	int ret = 0;
	if (handle == GNull) {
		return PARAM_INVALID;
	}

	// search in rb-tree
	pNode = rbt_search(&handle->mRBRoot, hash);

	if (pNode != GNull) {
		cache_data_parse(&(pNode->mData), width, height, format, GNull);
		return GOK;
	}

	return NOT_FIND;
}

int PullCache(unsigned long h, int hash, int *width, int *height, int *format, unsigned char ** data)
{
	LPCACHE_NODE pNode;
	LPCACHE_HANDLE handle = (LPCACHE_HANDLE)h;
	int ret = 0;
	if (handle == GNull) {
		return PARAM_INVALID;
	}

	// search in rb-tree
	pNode = rbt_search(&handle->mRBRoot, hash);

	if (pNode != GNull) {
		//remove out.
		dl_remove_node(&(pNode->mDLNode), &(handle->mDLRoot));

		//add node
		dl_insert_node(&(pNode->mDLNode), GNull, &(handle->mDLRoot));

		cache_data_parse(&(pNode->mData), width, height, format, data);

	} else {
		//not found.
#if defined( _DEBUG )
		LPRB_NODE node;
		LPDLL_NODE link;
		LPCACHE_NODE data;
		LOGI("not found %ld\n", hash);
		for (node = rb_first(&(handle->mRBRoot)); node != GNull; node = rb_next(node)) {
			container_of(data, node, CACHE_NODE, mRBNode);
			LOGI("%ld\n", data->mKey);
		}
		LOGI("double link list:\n");
		for (link = dll_first(&(handle->mDLLRoot)); link != GNull; link = dll_next(link)) {
			container_of(data, link, CACHE_NODE, mDLLNode);
			LOGI("%ld\n", data->mKey);
		}

#endif
		return -1;
	}

	return ret;
}

int ReleaseCache(unsigned long h)
{
	LPCACHE_NODE pNode = GNull;
	LPCACHE_NODE pFree = GNull;
	LPCACHE_HANDLE handle = (LPCACHE_HANDLE)h;
	LPDL_NODE node;
	int ret = 0;
	if (handle == GNull) {
		return -1;
	}

	node = dl_first(&(handle->mDLRoot));
	while (node) {
		container_of(pNode, node, CACHE_NODE, mDLNode);
		node = dl_next(node);
		cache_data_release(&(pNode->mData));
		GMemFree(pNode);
	}

	GMemFree(handle);

	return ret;
}

LPCACHE_NODE rbt_search(LPRB_ROOT root, int hash)
{
	LPRB_NODE node = root->rb_node;
	while (node) {
		LPCACHE_NODE this;
		container_of(this, node, CACHE_NODE, mRBNode);
		if (hash < this->mKey) {
			node = node->rb_left;
		} else if (hash > this->mKey) {
			node = node->rb_right;
		} else {
			return this;
		}
	}
	return GNull;
}

int rbt_insert(LPRB_ROOT root, LPCACHE_NODE data)
{
  	LPRB_NODE *new = &(root->rb_node);
	LPRB_NODE parent = NULL;

  	/* Figure out where to put new node */
  	while (*new) {
  		LPCACHE_NODE this;
  		container_of(this, *new, CACHE_NODE, mRBNode);
		parent = *new;
  		if (data->mKey < this->mKey) {
  			new = &((*new)->rb_left);
  		} else if (data->mKey > this->mKey) {
  			new = &((*new)->rb_right);
  		} else {
  			return 0;
  		}
  	}

  	/* Add new node and rebalance tree. */
  	rb_link_node(&data->mRBNode, parent, new);
  	rb_insert_color(&data->mRBNode, root);

	return 1;
}
