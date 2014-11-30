#include <stdio.h>
#include "dllist.h"

void dll_init_node(LPDLL_NODE node)
{
	node->dll_next = NULL;
	node->dll_pre = NULL;
}

void dll_insert_node(LPDLL_NODE node, LPDLL_NODE insert, LPDLL_ROOT root)
{
	if (insert) { //update head
		node->dll_next = insert->dll_next;
		insert->dll_next = node;
		node->dll_pre = insert;
		if (node->dll_next) {
			node->dll_next->dll_pre = node;
		} else { //update last
			root->dll_last = node;
		}
	} else {
		if (root->dll_head) {
			root->dll_head->dll_pre = node;
			node->dll_next = root->dll_head;
			root->dll_head = node;
			root->dll_head->dll_pre = NULL;
		} else {
			root->dll_head = node;
			node->dll_pre = NULL;
			root->dll_last = node;
			node->dll_next = NULL;
		}
	}
}

void dll_remove_node(LPDLL_NODE node, LPDLL_ROOT root)
{
	if (node->dll_pre) {
		node->dll_pre->dll_next = node->dll_next;
	} else {
		root->dll_head = node->dll_next;
	}

	if (node->dll_next) {
		node->dll_next->dll_pre = node->dll_pre;
	} else {
		root->dll_last = node->dll_pre;
	}
}

void dll_replace_node(LPDLL_NODE node, LPDLL_NODE replace, LPDLL_ROOT root)
{
	if (node->dll_pre) {
		node->dll_pre->dll_next = replace;
		replace->dll_pre = node->dll_pre;
	} else {
		root->dll_head = replace;
	}

	if (node->dll_next) {
		node->dll_next->dll_pre = replace;
		replace->dll_next = node->dll_next;
	} else {
		root->dll_last = replace;
	}
}

LPDLL_NODE dll_next(LPDLL_NODE node)
{
	return node->dll_next;
}

LPDLL_NODE dll_prev(LPDLL_NODE node)
{
	return node->dll_pre;
}

LPDLL_NODE dll_first(const LPDLL_ROOT root)
{
	return root->dll_head;
}

LPDLL_NODE dll_last(const LPDLL_ROOT root)
{
	return root->dll_last;
}
