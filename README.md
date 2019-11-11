Redfield AB Anonymization extension for KNIME
=============================================
Redfield developed a number of nodes to help the community with comply with GDPR. The nodes along with reference workflows help achieve the following objectives:
* Basic anonymization and pseudonymization
* Advanced hierarchical anonymization
* Assessment of the anonymization (reduce re-identification risks)

Nodes Summary
--------------
* __Anonymization__ - these nodes utilizes SHA-1 hashing with several types of salting techniques.
* __Assessment__ - estimates the distinction and separation for quasi-identifying attributes and three attacker models: journalist, marketer and prosecutor.
* __Hierarchical Anonymization__ - Different hierarchical types can be applied:
  * order-based;
  * date-based;
  * interval-based;
  * masking-base.

  Once hierarchical type is selected it is possible to tweak anonymization strategy.
* __Create hierarchy__ - This node is used for building the hierarchies that are used in Anonymization node. 
* __Hierarchy Reader__ - This node has two main functions:
  * read the binary file of hierarchy;
  * update the hierarchy to fit the input data set.
* __Hierarchy writer__ - Created or updated hierarchies can be stored on the disk as binary files with the help of this node.

License
--------
Copyright (c) 2019 Redfield AB.
This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3, see the [LICENSE](https://www.gnu.org/licenses/gpl-3.0.html) for details.