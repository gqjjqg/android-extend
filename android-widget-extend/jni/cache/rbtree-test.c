/*
 * =============================================================================
 *
 *       Filename:  rbtree-tst.c
 *
 *    Description:  rbtree testcase.
 *
 *        Created:  09/02/2012 11:39:34 PM
 *
 *         Author:  Fu Haiping (forhappy), haipingf@gmail.com
 *        Company:  ICT ( Institute Of Computing Technology, CAS )
 *
 * =============================================================================
 */

#include "rbtree.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct mynode {
	RB_NODE node;
  	char *string;
};

RB_ROOT mytree = RB_ROOT;

struct mynode * my_search(LPRB_ROOT root, char *string)
{
  	LPRB_NODE node = root->rb_node;

  	while (node) {
  		struct mynode *data;
		int result;
		container_of(data, node, struct mynode, node);
		result = strcmp(string, data->string);

		if (result < 0)
  			node = node->rb_left;
		else if (result > 0)
  			node = node->rb_right;
		else
  			return data;
	}
	return NULL;
}

int my_insert(LPRB_ROOT root, struct mynode *node)
{
  	LPRB_NODE *new = &(root->rb_node);
	LPRB_NODE parent = NULL;

  	/* Figure out where to put new node */
  	while (*new) {
  		struct mynode *data;
		int result;
		container_of(data, *new, struct mynode, node);
		result = strcmp(node->string, data->string);

		parent = *new;
  		if (result < 0)
  			new = &((*new)->rb_left);
  		else if (result > 0)
  			new = &((*new)->rb_right);
  		else
  			return 0;
  	}

  	/* Add new node and rebalance tree. */
  	rb_link_node(&node->node, parent, new);
  	rb_insert_color(&node->node, root);

	return 1;
}

void my_free(struct mynode *node)
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

int main()
{

	struct mynode *mn[NUM_NODES];
	LPRB_NODE node;
	struct mynode *data;

	/* *insert */
	int i = 0;
	printf("insert node from 1 to NUM_NODES(32): \n");
	for (; i < NUM_NODES; i++) {
		mn[i] = (struct mynode *)malloc(sizeof(struct mynode));
		mn[i]->string = (char *)malloc(sizeof(char) * 4);
		sprintf(mn[i]->string, "%d", i);
		my_insert(&mytree, mn[i]);
	}
	
	/* *search */
	printf("search all nodes: \n");
	for (node = rb_first(&mytree); node; node = rb_next(node)) {
		struct mynode *data;
		container_of(data, node, struct mynode, node);
		printf("key = %s\n", data->string);
	}

	/* *delete */
	printf("delete node 20: \n");
	data = my_search(&mytree, "20");
	if (data) {
		rb_erase(&data->node, &mytree);
		my_free(data);
	}

	/* *delete again*/
	printf("delete node 10: \n");
	data = my_search(&mytree, "10");
	if (data) {
		rb_erase(&data->node, &mytree);
		my_free(data);
	}

	/* *delete once again*/
	printf("delete node 15: \n");
	data = my_search(&mytree, "15");
	if (data) {
		rb_erase(&data->node, &mytree);
		my_free(data);
	}

	/* *search again*/
	printf("search again:\n");
	for (node = rb_first(&mytree); node; node = rb_next(node)) {
		struct mynode *data;
		container_of(data, node, struct mynode, node);
		printf("key = %s\n", data->string);
	}
	return 0;
}


