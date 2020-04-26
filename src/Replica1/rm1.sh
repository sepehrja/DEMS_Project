#!/bin/bash

java Sequencer.Sequencer &> seq.txt
java Replica1.RM1 &> rm1.txt
java Replica1.Server.Server &