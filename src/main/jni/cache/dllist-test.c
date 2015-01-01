/*
 * =============================================================================
 *
 *       Filename:  dllist-test.c
 *
 *    Description:  dllist testcase.
 *
 *        Created:  11/30/2014 11:39:34 PM
 *
 *         Author:  Guo QiJiang (gqjjqg), qijiang.guo@gmail.com
 *        Company:  
 *
 * =============================================================================
 */

#include "dllist.h"
//#include "CDebug.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


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

struct mynode {
	DL_NODE node;
  	char *string;
};

#define Debug_Message(x, ...)		printf(__VA_ARGS__)
#define LOG_TEST
#define LOG_FULL

static DL_ROOT mytree = {0,0};

struct mynode * dll_search(LPDL_ROOT root, char *string)
{
  	LPDL_NODE node = dl_first(root);

  	while (node) {
  		struct mynode *data;
		int result;
		container_of(data, node, struct mynode, node);
		result = strcmp(string, data->string);
		
		if (result != 0) {
  			node = node->dl_next;
		} else {
  			return data;
		}
	}
	return NULL;
}

int dll_insert(LPDL_ROOT root, struct mynode *node)
{
  	LPDL_NODE last = dl_last(root);

	dl_insert_node(&(node->node), last, root);

	return 0;
}

void dll_free(struct mynode *node)
{
	if (node != NULL) {
		if (node->string != NULL) {
			free(node->string);
			node->string = NULL;
		}
		free(node);
		node = NULL;
	}
}

#define NUM_NODES 32

int dllist_test()
{

	struct mynode *mn[NUM_NODES];
	LPDL_NODE node;
	struct mynode *data;

	/* *insert */
	int i = 0;
	Debug_Message(LOG_FULL, "insert node from 1 to NUM_NODES(32): \n");
	for (; i < NUM_NODES; i++) {
		mn[i] = (struct mynode *)malloc(sizeof(struct mynode));
		mn[i]->string = (char *)malloc(sizeof(char) * 4);
		sprintf(mn[i]->string, "%d", i);
		dll_insert(&mytree, mn[i]);
	}
	
	/* *search */
	Debug_Message(LOG_FULL, "search all nodes: \n");
	for (node = dl_first(&mytree); node; node = dl_next(node)) {
		struct mynode *data;
		container_of(data, node, struct mynode, node);
		Debug_Message(LOG_TEST, "key = %s\n", data->string);
	}

	/* *delete */
	Debug_Message(LOG_FULL, "delete node 0: \n");
	data = dll_search(&mytree, "0");
	if (data) {
		dl_remove_node(&data->node, &mytree);
		dll_free(data);
	}

	/* *delete again*/
	Debug_Message(LOG_FULL, "delete node 10: \n");
	data = dll_search(&mytree, "10");
	if (data) {
		dl_remove_node(&data->node, &mytree);
		dll_free(data);
	}

	/* *delete once again*/
	Debug_Message(LOG_FULL, "delete node 31: \n");
	data = dll_search(&mytree, "31");
	if (data) {
		dl_remove_node(&data->node, &mytree);
		dll_free(data);
	}

	/* *search again*/
	Debug_Message(LOG_FULL, "search again:\n");
	for (node = dl_first(&mytree); node; node = dl_next(node)) {
		struct mynode *data;
		container_of(data, node, struct mynode, node);
		Debug_Message(LOG_TEST,"key = %s\n", data->string);
	}
	return 0;
}


